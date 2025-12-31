# GrowCare - Comprehensive Project Architecture Analysis

**Project**: GrowCare - Agricultural Management Android Application  
**Package**: `com.example.growCare`  
**Architecture Pattern**: Clean Architecture with MVVM  
**Target SDK**: 36 | Min SDK: 24  
**Analysis Date**: December 16, 2025

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Architectural Overview](#architectural-overview)
3. [Layer-by-Layer Analysis](#layer-by-layer-analysis)
4. [Design Patterns](#design-patterns)
5. [Dependency Management](#dependency-management)
6. [Technology Stack](#technology-stack)
7. [Data Flow Architecture](#data-flow-architecture)
8. [Component Communication](#component-communication)
9. [Cross-Cutting Concerns](#cross-cutting-concerns)
10. [Best Practices Implementation](#best-practices-implementation)

---

## Executive Summary

GrowCare is built using **Clean Architecture** principles combined with the
**MVVM (Model-View-ViewModel)** pattern, creating a highly maintainable,
testable, and scalable Android application. The architecture emphasizes
separation of concerns, dependency inversion, and unidirectional data flow.

### Key Architectural Characteristics:

- **Clean Architecture**: Three distinct layers (Presentation, Domain, Data)
- **MVVM Pattern**: ViewModel-driven UI with reactive state management
- **Dependency Injection**: Hilt/Dagger for complete IoC (Inversion of Control)
- **Reactive Programming**: Kotlin Coroutines and Flow for asynchronous
  operations
- **Single Source of Truth**: StateFlow-based state management
- **Repository Pattern**: Abstraction layer between data sources and business
  logic
- **Use Case Pattern**: Encapsulated business logic operations
- **Unidirectional Data Flow**: Clear data flow from View → ViewModel → Use Case
  → Repository

---

## Architectural Overview

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                      │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐             │
│  │  Screens   │  │ ViewModels │  │ Components │             │
│  │ (Compose)  │◄─┤ (StateFlow)│◄─┤  (Reusable)│             │
│  └────────────┘  └────────────┘  └────────────┘             │
│         ▲               │                                   │
│         │               ▼                                   │
│    User Actions    UI State/Events                          │
└─────────┼──────────────┼────────────────────────────────────┘
          │              │
          │              ▼
┌─────────┴──────────────────────────────────────────────────┐
│                      DOMAIN LAYER                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │  Use Cases │  │    Models  │  │ Repository │            │
│  │ (Business  │◄─┤  (Domain)  │◄─┤ Interfaces │            │
│  │   Logic)   │  └────────────┘  └────────────┘            │
│  └────────────┘                                            │
│         │                                                  │
│         ▼                                                  │
└─────────┼──────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                            │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐             │
│  │ Repository │  │   Remote   │  │   Local    │             │
│  │    Impl    │◄─┤   Sources  │  │   Sources  │             │
│  └────────────┘  └────────────┘  └────────────┘             │
│                       │                  │                  │
│                       ▼                  ▼                  │
│              ┌──────────────┐   ┌──────────────┐            │
│              │   Firebase   │   │ Room Database│            │
│              │  Gemini AI   │   │  DataStore   │            │
│              │  Weather API │   │  Preferences │            │
│              └──────────────┘   └──────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

### Dependency Rule

**Critical Principle**: Dependencies point **inward** only.

- Presentation Layer depends on Domain Layer
- Domain Layer depends on **nothing** (pure business logic)
- Data Layer depends on Domain Layer (implements interfaces)

This ensures:

- Domain logic is framework-independent
- Easy testing with mock implementations
- Flexibility to change frameworks without affecting business logic

---

## Layer-by-Layer Analysis

## 1. Presentation Layer

### Purpose

Handles all UI-related code and user interactions. Built entirely with **Jetpack
Compose** (no XML layouts).

### Structure

```
presentation/
├── screens/              # Feature-based screen organization
│   ├── auth/            # Login, SignUp
│   ├── home/            # Dashboard
│   ├── chat/            # AI Assistant
│   ├── fertilizer/      # Fertilizer Calculator
│   ├── detection/       # Disease Detection
│   ├── seed/            # Seed Quality Scanner
│   ├── profile/         # User Profile
│   └── history/         # Activity History
│
├── components/          # Reusable UI components
│   ├── CameraCapture.kt
│   ├── PrimaryButton.kt
│   ├── AnimatedComponents.kt
│   └── ThemeToggle.kt
│
├── navigation/          # Navigation configuration
│   ├── NavGraph.kt      # Navigation graph definition
│   ├── Screen.kt        # Screen route constants
│   └── NavigationViewModel.kt
│
└── theme/               # UI theming
    ├── ThemeViewModel.kt
    └── Material3 theme config
```

### Key Components

#### 1. Screens (Composables)

Each screen follows this pattern:

```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel(),
    onNavigateToX: () -> Unit
) {
    // Collect UI state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FeatureEvent.NavigateBack -> onNavigateToX()
                is FeatureEvent.ShowError -> /* show snackbar */
            }
        }
    }

    // Render UI
    FeatureContent(uiState, viewModel::onAction)
}
```

**Key Characteristics**:

- Pure UI logic (no business logic)
- State-driven rendering
- Event-based navigation
- ViewModel injection via Hilt

#### 2. ViewModels

Architecture: **MVVM with StateFlow**

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val useCase: FeatureUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // UI State - single source of truth
    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    // One-time events (navigation, snackbars)
    private val _events = MutableSharedFlow<FeatureEvent>()
    val events: SharedFlow<FeatureEvent> = _events.asSharedFlow()

    // Action handler
    fun onAction(action: FeatureAction) {
        when (action) {
            is FeatureAction.LoadData -> loadData()
            is FeatureAction.Submit -> submitData(action.data)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            useCase()
                .onSuccess { data ->
                    _uiState.update { it.copy(data = data, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }
}
```

**State Management Pattern**:

- **StateFlow**: Continuous state (data, loading, errors)
- **SharedFlow**: One-time events (navigation, messages)
- **Actions**: User interactions encapsulated as sealed interfaces
- **Immutable State**: State updates via `copy()` method

#### 3. Navigation

Architecture: **Type-Safe Navigation with Jetpack Navigation Compose**

```kotlin
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.HOME
) {
    NavHost(navController, startDestination) {
        composable(Screen.HOME) {
            HomeScreen(
                onNavigateToFertilizer = {
                    navController.navigate(Screen.FERTILIZER)
                }
            )
        }

        composable(
            route = Screen.DISEASE_RESULT,
            arguments = listOf(navArgument("analysisId") { type = NavType.StringType })
        ) { backStackEntry ->
            val analysisId = backStackEntry.arguments?.getString("analysisId")
            DiseaseResultScreen(analysisId)
        }
    }
}
```

**Navigation Features**:

- Centralized navigation graph
- Type-safe arguments
- Deep linking support
- Back stack management
- Authentication-based routing

---

## 2. Domain Layer

### Purpose

Contains **pure business logic** with **zero dependencies** on Android framework
or external libraries. This is the core of the application.

### Structure

```
domain/
├── model/              # Domain models (POKOs)
│   ├── User.kt
│   ├── ChatMessage.kt
│   ├── DiseaseAnalysis.kt
│   ├── FertilizerRecommendation.kt
│   ├── SeedQuality.kt
│   ├── CropData.kt
│   └── WeatherData.kt
│
├── repository/         # Repository interfaces (contracts)
│   ├── AuthRepository.kt
│   ├── ChatRepository.kt
│   ├── DetectionRepository.kt
│   ├── FertilizerRepository.kt
│   ├── WeatherRepository.kt
│   ├── CropRepository.kt
│   └── UserRepository.kt
│
└── usecase/           # Business logic operations
    ├── auth/
    │   ├── SignInUseCase.kt
    │   └── SignUpUseCase.kt
    ├── chat/
    │   ├── SendChatMessageUseCase.kt
    │   └── GetChatHistoryUseCase.kt
    ├── detection/
    │   ├── AnalyzePlantDiseaseUseCase.kt
    │   └── AnalyzeSeedQualityUseCase.kt
    └── fertilizer/
        └── CalculateFertilizerUseCase.kt
```

### Key Components

#### 1. Domain Models

Pure data classes with no annotations or framework dependencies:

```kotlin
data class User(
    val uid: String,
    val email: String,
    val displayName: String? = null,
    val profilePictureUrl: String? = null,
    val farmSize: Double? = null,
    val preferredCrops: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
```

**Characteristics**:

- Immutable data classes
- Business-relevant properties only
- No Android/Firebase types
- Framework-agnostic

#### 2. Repository Interfaces

Define contracts for data operations:

```kotlin
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, displayName: String?): Result<User>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): User?
    fun isAuthenticated(): Boolean
    fun observeAuthState(): Flow<Boolean>
}
```

**Design Principles**:

- Interface segregation (focused contracts)
- Framework-agnostic return types
- Kotlin Result type for error handling
- Flow for reactive streams

#### 3. Use Cases

Encapsulate single business operations:

```kotlin
class AnalyzePlantDiseaseUseCase @Inject constructor(
    private val repository: DetectionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        imageUri: Uri,
        cropName: String? = null
    ): Result<DiseaseAnalysis> = withContext(dispatcher) {
        try {
            repository.analyzePlantDisease(imageUri, cropName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Use Case Pattern Benefits**:

- **Single Responsibility**: One use case = one business operation
- **Reusability**: Can be used across multiple ViewModels
- **Testability**: Easy to mock and test in isolation
- **Thread Management**: Explicit dispatcher injection
- **invoke() operator**: Enables function-like syntax: `useCase(params)`

---

## 3. Data Layer

### Purpose

Handles all data operations: remote API calls, local database, caching, and data
mapping.

### Structure

```
data/
├── repository/         # Repository implementations
│   ├── AuthRepositoryImpl.kt
│   ├── ChatRepositoryImpl.kt
│   ├── DetectionRepositoryImpl.kt
│   ├── FertilizerRepositoryImpl.kt
│   └── WeatherRepositoryImpl.kt
│
├── remote/            # Remote data sources
│   ├── firebase/
│   │   ├── FirebaseAuthDataSource.kt
│   │   ├── FirestoreDataSource.kt
│   │   └── FirebaseStorageDataSource.kt
│   ├── gemini/
│   │   ├── GeminiClient.kt
│   │   └── GeminiAIService.kt
│   ├── weather/
│   │   ├── WeatherApiService.kt
│   │   └── OpenWeatherModels.kt
│   └── location/
│       └── LocationDataSource.kt
│
├── local/             # Local data sources
│   ├── database/
│   │   ├── dao/       # Room DAOs (to be implemented)
│   │   └── entity/    # Room entities (to be implemented)
│   ├── preferences/
│   │   └── ThemePreferences.kt
│   └── location/
│       └── LocationService.kt
│
└── mapper/            # Data ↔ Domain mapping
    └── WeatherMapper.kt
```

### Key Components

#### 1. Repository Implementation

Implements domain repository interfaces:

```kotlin
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<User> {
        return authDataSource.signInWithEmail(email, password)
            .mapCatching { firebaseUser ->
                // Get additional user data from Firestore
                val userData = firestoreDataSource.getUserData(firebaseUser.uid).getOrNull()
                firebaseUser.toUser(userData)
            }
    }

    override suspend fun signUp(email: String, password: String, displayName: String?): Result<User> {
        return authDataSource.signUpWithEmail(email, password, displayName ?: "")
            .mapCatching { firebaseUser ->
                // Create user profile in Firestore
                val user = firebaseUser.toUser()
                firestoreDataSource.createUserProfile(user)
                user
            }
    }
}
```

**Repository Pattern Benefits**:

- **Abstraction**: Hides data source complexity
- **Multiple Data Sources**: Combines remote + local seamlessly
- **Caching Strategy**: Can implement cache-first logic
- **Error Handling**: Centralized error management
- **Testing**: Easy to mock for unit tests

#### 2. Remote Data Sources

##### Firebase Integration

```kotlin
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        if (result.user != null) {
            Result.success(result.user!!)
        } else {
            Result.failure(Exception("Sign in failed"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Firebase Services Used**:

- **Firebase Authentication**: User management
- **Cloud Firestore**: Document database for user profiles, history
- **Firebase Storage**: Image storage for disease/seed scans

##### Gemini AI Integration

```kotlin
@Singleton
class GeminiClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun sendMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(message)
            Result.success(response.text ?: "No response")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeImage(imageUri: Uri, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmapFromUri(context, imageUri)
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            val response = generativeModel.generateContent(inputContent)
            Result.success(response.text ?: "No analysis")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getChatStream(messages: List<ChatMessage>): Flow<String> = flow {
        val chat = generativeModel.startChat(
            history = messages.dropLast(1).map { msg ->
                content(role = if (msg.isUser) "user" else "model") {
                    text(msg.content)
                }
            }
        )

        val lastMessage = messages.lastOrNull()?.content ?: return@flow

        chat.sendMessageStream(lastMessage).collect { chunk ->
            emit(chunk.text ?: "")
        }
    }
}
```

**Gemini AI Features**:

- Text generation for agricultural advice
- Image analysis for disease detection
- Streaming responses for chat
- Context-aware conversations

##### Weather API Integration

```kotlin
interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val locationDataSource: LocationDataSource,
    private val weatherMapper: WeatherMapper
) : WeatherRepository {

    override fun getCurrentWeather(): Flow<WeatherData> = flow {
        val location = locationDataSource.getCurrentLocation()
        val response = weatherApiService.getCurrentWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            apiKey = BuildConfig.WEATHER_API_KEY
        )
        emit(weatherMapper.toDomain(response))
    }
}
```

#### 3. Local Data Sources

##### DataStore (Preferences)

```kotlin
@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("theme_prefs")

    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            when (preferences[THEME_MODE_KEY]) {
                "DARK" -> ThemeMode.DARK
                "LIGHT" -> ThemeMode.LIGHT
                else -> ThemeMode.SYSTEM
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
}
```

##### Room Database (Planned)

```kotlin
// To be implemented - structure prepared
@Database(entities = [CropEntity::class, ChatMessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cropDao(): CropDao
    abstract fun chatDao(): ChatDao
}

@Dao
interface CropDao {
    @Query("SELECT * FROM crops WHERE userId = :userId")
    fun getCrops(userId: String): Flow<List<CropEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity)
}
```

#### 4. Mappers

Convert between data models and domain models:

```kotlin
class WeatherMapper @Inject constructor() {
    fun toDomain(response: WeatherResponse): WeatherData {
        return WeatherData(
            temperature = response.main.temp,
            feelsLike = response.main.feelsLike,
            humidity = response.main.humidity,
            description = response.weather.firstOrNull()?.description ?: "",
            windSpeed = response.wind.speed,
            cityName = response.name,
            timestamp = System.currentTimeMillis()
        )
    }
}
```

---

## Design Patterns

### 1. **Clean Architecture**

**Implementation**: Three-layer separation (Presentation, Domain, Data)

**Benefits**:

- Framework independence
- Testability at every layer
- Business logic isolation
- Easy to modify or replace components

### 2. **MVVM (Model-View-ViewModel)**

**Implementation**: ViewModels manage UI state, Views observe state

**Benefits**:

- Separation of UI and business logic
- Lifecycle-aware components
- Reactive UI updates
- Easier testing of UI logic

### 3. **Repository Pattern**

**Implementation**: Abstract data access through repository interfaces

**Benefits**:

- Single source of truth for data
- Centralized caching strategy
- Easy data source switching
- Simplified testing with mocks

### 4. **Use Case Pattern (Interactor)**

**Implementation**: Each business operation encapsulated in a use case

**Benefits**:

- Single responsibility principle
- Reusable business logic
- Easy to test independently
- Clear business operation boundaries

### 5. **Dependency Injection (IoC)**

**Implementation**: Hilt/Dagger for compile-time DI

**Benefits**:

- Loose coupling
- Easy testing with mock dependencies
- Centralized dependency configuration
- Compile-time error detection

### 6. **Observer Pattern (Reactive)**

**Implementation**: Flow and StateFlow for reactive streams

**Benefits**:

- Automatic UI updates on data changes
- Efficient resource management
- Backpressure handling
- Cancellation support

### 7. **Factory Pattern**

**Implementation**: Hilt modules provide instances

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
```

### 8. **Strategy Pattern**

**Implementation**: Different data source strategies (Remote/Local)

```kotlin
class CropRepositoryImpl @Inject constructor(
    private val remoteDataSource: CropRemoteDataSource,
    private val localDataSource: CropLocalDataSource
) : CropRepository {
    override suspend fun getCrops(): Result<List<Crop>> = try {
        // Try remote first (online-first strategy)
        val crops = remoteDataSource.fetchCrops()
        localDataSource.saveCrops(crops) // Cache
        Result.success(crops)
    } catch (e: IOException) {
        // Fallback to cache (offline-first fallback)
        val cachedCrops = localDataSource.getCrops()
        if (cachedCrops.isNotEmpty()) {
            Result.success(cachedCrops)
        } else {
            Result.failure(NetworkException("No internet and no cache"))
        }
    }
}
```

### 9. **Adapter Pattern**

**Implementation**: Mappers convert between data and domain models

### 10. **Sealed Classes for Type Safety**

**Implementation**: Action, Event, and State patterns

```kotlin
sealed interface ChatAction {
    data class SendMessage(val message: String) : ChatAction
    data class SendMessageWithImage(val message: String, val imageUri: Uri) : ChatAction
    data object ClearChat : ChatAction
    data object StartNewChat : ChatAction
}

sealed interface ChatEvent {
    data class ShowError(val message: String) : ChatEvent
    data object NavigateBack : ChatEvent
    data class NavigateToResult(val id: String) : ChatEvent
}
```

---

## Dependency Management

### Dependency Injection Architecture

#### Hilt/Dagger Configuration

```kotlin
// Application-level setup
@HiltAndroidApp
class GrowCareApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity()

@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val useCase: FeatureUseCase
) : ViewModel()
```

#### Module Organization

**1. AppModule** - Core dependencies

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideContext(@ApplicationContext context: Context): Context

    @Provides @Singleton
    fun provideGson(): Gson

    @Provides @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

**2. NetworkModule** - Retrofit, OkHttp

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit

    @Provides @Singleton
    fun provideWeatherApiService(@Named("weather") retrofit: Retrofit): WeatherApiService
}
```

**3. FirebaseModule** - Firebase services

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
```

**4. DatabaseModule** - Room (future)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // To be implemented when Room database is added
}
```

**5. RepositoryModule** - Repository bindings

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}
```

### Dependency Scopes

- **@Singleton**: App-level lifecycle (repositories, network clients)
- **@ViewModelScoped**: ViewModel lifecycle (use cases in ViewModels)
- **@ActivityScoped**: Activity lifecycle (rarely used with Compose)

---

## Technology Stack

### Core Technologies

#### 1. **Kotlin (v2.0.21)**

- Primary programming language
- Coroutines for async operations
- Flow for reactive streams
- Null safety
- Extension functions

#### 2. **Jetpack Compose (BOM 2024.09.00)**

- Declarative UI framework
- Material3 design system
- No XML layouts
- State-driven UI
- Compose Navigation

#### 3. **Hilt (v2.52)**

- Dependency injection
- Compile-time DI
- ViewModel injection
- Component hierarchy

#### 4. **Kotlin Coroutines**

- Asynchronous programming
- Structured concurrency
- Flow for streams
- viewModelScope/lifecycleScope

### Backend Services

#### 1. **Firebase**

```kotlin
firebase-bom = "32.7.0"
├── firebase-auth-ktx          // Authentication
├── firebase-firestore-ktx     // NoSQL database
└── firebase-storage-ktx       // Cloud storage
```

**Usage**:

- User authentication (email/password)
- User profile storage
- Image upload (disease/seed scans)
- Analysis history

#### 2. **Gemini AI (v0.1.2)**

```kotlin
generativeai = "0.1.2"
```

**Features**:

- Agricultural chat assistant
- Plant disease diagnosis
- Seed quality analysis
- Image understanding
- Streaming responses

#### 3. **OpenWeather API**

```kotlin
Retrofit + OkHttp for REST API calls
```

**Integration**:

- Real-time weather data
- Location-based forecasts
- Agricultural insights

### Local Storage

#### 1. **DataStore Preferences (v1.1.1)**

- Theme preferences
- User settings
- Key-value storage

#### 2. **Room Database (v2.6.1)** - Planned

- Offline data caching
- Chat history
- Analysis history
- Crop data

### Networking

```kotlin
retrofit = "2.9.0"
okhttp = "4.12.0"

├── Retrofit                   // REST client
├── OkHttp                     // HTTP client
├── Gson Converter             // JSON parsing
└── Logging Interceptor        // Debug logging
```

### Camera & Image Processing

```kotlin
camerax = "1.3.1"

├── camera-camera2             // Camera2 API integration
├── camera-lifecycle           // Lifecycle awareness
└── camera-view                // Camera UI
```

### Image Loading

```kotlin
coil = "2.5.0"                 // Compose-first image loading
```

### Location Services

```kotlin
play-services-location = "21.0.1"
```

### Testing

```kotlin
├── JUnit                      // Unit testing
├── MockK                      // Mocking
├── Turbine                    // Flow testing
├── Coroutines Test            // Coroutine testing
└── Espresso/Compose Test      // UI testing
```

---

## Data Flow Architecture

### Unidirectional Data Flow (UDF)

```
┌─────────────────────────────────────────────────────────┐
│                    User Interaction                      │
│                    (Button Click)                        │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  Composable Screen                       │
│               onAction(UserAction)                       │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                     ViewModel                            │
│          Handle action → Call use case                   │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                      Use Case                            │
│            Execute business logic                        │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    Repository                            │
│        Fetch from remote/local sources                   │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   Data Sources                           │
│          Firebase / Gemini / Weather API                 │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  Result<Data>                            │
│             Return to Repository                         │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  Map to Domain                           │
│           Repository → Use Case                          │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                Update UI State                           │
│         ViewModel updates StateFlow                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                 Recomposition                            │
│            Composable observes state                     │
│               UI automatically updates                   │
└─────────────────────────────────────────────────────────┘
```

### Concrete Example: Sending a Chat Message

```kotlin
// 1. USER ACTION
Button(onClick = { viewModel.onAction(ChatAction.SendMessage("Hello")) })

// 2. VIEWMODEL
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendChatMessageUseCase
) : ViewModel() {

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.SendMessage -> sendMessage(action.message)
        }
    }

    private fun sendMessage(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }

            sendMessageUseCase(message, conversationId).collect { chatMessage ->
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages + chatMessage,
                        isSending = chatMessage.isStreaming
                    )
                }
            }
        }
    }
}

// 3. USE CASE
class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(message: String, conversationId: String): Flow<ChatMessage> {
        return chatRepository.sendMessage(message, conversationId)
    }
}

// 4. REPOSITORY
class ChatRepositoryImpl @Inject constructor(
    private val geminiClient: GeminiClient,
    private val firestoreDataSource: FirestoreDataSource
) : ChatRepository {

    override suspend fun sendMessage(message: String, conversationId: String): Flow<ChatMessage> = flow {
        // Save user message
        val userMessage = ChatMessage(id = UUID.randomUUID().toString(), content = message, isUser = true)
        firestoreDataSource.saveChatMessage(conversationId, userMessage)
        emit(userMessage)

        // Get AI response (streaming)
        val history = firestoreDataSource.getChatHistory(conversationId)
        val aiResponseBuilder = StringBuilder()

        geminiClient.getChatStream(history + userMessage).collect { chunk ->
            aiResponseBuilder.append(chunk)
            emit(ChatMessage(
                id = UUID.randomUUID().toString(),
                content = aiResponseBuilder.toString(),
                isUser = false,
                isStreaming = true
            ))
        }

        // Save final AI message
        val aiMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = aiResponseBuilder.toString(),
            isUser = false,
            isStreaming = false
        )
        firestoreDataSource.saveChatMessage(conversationId, aiMessage)
        emit(aiMessage)
    }
}

// 5. DATA SOURCE
class GeminiClient @Inject constructor() {
    fun getChatStream(messages: List<ChatMessage>): Flow<String> = flow {
        val chat = generativeModel.startChat(...)
        chat.sendMessageStream(message).collect { chunk ->
            emit(chunk.text ?: "")
        }
    }
}

// 6. UI OBSERVES STATE
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn {
        items(uiState.messages) { message ->
            MessageBubble(message)
        }
    }
}
```

---

## Component Communication

### 1. **View ↔ ViewModel Communication**

#### View to ViewModel (Actions)

```kotlin
// Sealed interface for type-safe actions
sealed interface ChatAction {
    data class SendMessage(val message: String) : ChatAction
    data object ClearChat : ChatAction
}

// ViewModel exposes single action handler
class ChatViewModel {
    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.SendMessage -> sendMessage(action.message)
            ChatAction.ClearChat -> clearChat()
        }
    }
}

// Composable sends actions
Button(onClick = { viewModel.onAction(ChatAction.SendMessage(text)) })
```

#### ViewModel to View (State & Events)

```kotlin
// State: Continuous data
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null
)

val uiState: StateFlow<ChatUiState>

// Events: One-time actions
sealed interface ChatEvent {
    data class ShowSnackbar(val message: String) : ChatEvent
    data object NavigateBack : ChatEvent
}

val events: SharedFlow<ChatEvent>

// Composable observes both
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                ChatEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }
}
```

### 2. **ViewModel ↔ Use Case Communication**

```kotlin
// ViewModel calls use case
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendChatMessageUseCase
) {
    private fun sendMessage(message: String) {
        viewModelScope.launch {
            sendMessageUseCase(message, conversationId)
                .collect { chatMessage ->
                    _uiState.update { it.copy(messages = it.messages + chatMessage) }
                }
        }
    }
}
```

### 3. **Use Case ↔ Repository Communication**

```kotlin
// Use case calls repository
class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(message: String, conversationId: String): Flow<ChatMessage> {
        return repository.sendMessage(message, conversationId)
    }
}
```

### 4. **Repository ↔ Data Source Communication**

```kotlin
// Repository coordinates multiple data sources
class ChatRepositoryImpl @Inject constructor(
    private val geminiClient: GeminiClient,       // Remote
    private val firestoreDataSource: FirestoreDataSource, // Remote
    private val chatDao: ChatDao                  // Local (future)
) {
    override suspend fun sendMessage(message: String, conversationId: String): Flow<ChatMessage> = flow {
        // Save user message locally
        chatDao.insertMessage(message.toEntity())

        // Get AI response
        geminiClient.getChatStream(messages).collect { chunk ->
            emit(chunk)
        }

        // Save AI response to Firestore
        firestoreDataSource.saveChatMessage(conversationId, aiMessage)
    }
}
```

### 5. **Screen ↔ Screen Communication (Navigation)**

```kotlin
// Type-safe navigation with arguments
composable(
    route = Screen.DISEASE_RESULT + "/{analysisId}",
    arguments = listOf(navArgument("analysisId") { type = NavType.StringType })
) { backStackEntry ->
    val analysisId = backStackEntry.arguments?.getString("analysisId")
    DiseaseResultScreen(analysisId)
}

// Navigate with arguments
onNavigateToResult = { analysisId ->
    navController.navigate("${Screen.DISEASE_RESULT}/$analysisId")
}
```

---

## Cross-Cutting Concerns

### 1. **Error Handling**

#### Consistent Error Pattern

```kotlin
// Use Kotlin Result type throughout
suspend fun getData(): Result<Data> = try {
    Result.success(dataSource.fetch())
} catch (e: Exception) {
    Result.failure(e)
}

// ViewModel handles errors uniformly
private fun loadData() {
    viewModelScope.launch {
        useCase()
            .onSuccess { data ->
                _uiState.update { it.copy(data = data, error = null) }
            }
            .onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
                _events.emit(ShowError(error.message))
            }
    }
}
```

#### Custom Exception Hierarchy

```kotlin
sealed class AppException(message: String) : Exception(message) {
    class NetworkException(message: String = "Network error") : AppException(message)
    class AuthException(message: String = "Authentication failed") : AppException(message)
    class ValidationException(message: String = "Validation failed") : AppException(message)
}
```

### 2. **Logging**

```kotlin
// OkHttp logging interceptor for network calls
@Provides
fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}
```

### 3. **Configuration Management**

```kotlin
// Build-time configuration via BuildConfig
buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
buildConfigField("String", "WEATHER_API_KEY", "\"$weatherApiKey\"")

// Runtime access
val apiKey = BuildConfig.GEMINI_API_KEY

// Stored securely in local.properties (not in version control)
```

### 4. **Permission Handling**

```kotlin
// Declarative permission requests in Compose
val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // Permission granted
    } else {
        // Permission denied
    }
}

Button(onClick = {
    permissionLauncher.launch(Manifest.permission.CAMERA)
})
```

**Permissions Used**:

- `CAMERA`: Disease detection, seed scanning
- `ACCESS_FINE_LOCATION`: Weather data
- `INTERNET`: API calls

### 5. **Theming**

```kotlin
// Theme management with DataStore
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }
}

// Dynamic theming in MainActivity
setContent {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val isDarkTheme = shouldUseDarkTheme(themeMode)

    MobileAppDevTheme(darkTheme = isDarkTheme) {
        NavGraph(...)
    }
}
```

### 6. **Resource Management**

```kotlin
// Lifecycle-aware coroutines
viewModelScope.launch {
    // Automatically cancelled when ViewModel is cleared
}

// Proper Flow cancellation
DisposableEffect(Unit) {
    val listener = registerListener()
    onDispose {
        listener.remove()
    }
}
```

---

## Best Practices Implementation

### 1. **Single Source of Truth**

```kotlin
// UI State is the single source of truth
private val _uiState = MutableStateFlow(FeatureUiState())
val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

// All UI updates go through state
_uiState.update { currentState ->
    currentState.copy(data = newData)
}
```

### 2. **Immutability**

```kotlin
// Immutable data classes
data class User(
    val uid: String,
    val email: String,
    val displayName: String?
)

// State updates via copy()
_uiState.update { it.copy(isLoading = true) }
```

### 3. **Null Safety**

```kotlin
// Explicit nullability
fun getCurrentUser(): User?

// Safe calls and Elvis operator
val name = user?.displayName ?: "Unknown"

// Null checks with let
user?.let { saveUser(it) }
```

### 4. **Coroutine Best Practices**

```kotlin
// Use appropriate dispatchers
suspend fun loadData() = withContext(Dispatchers.IO) {
    // IO operations
}

// viewModelScope for automatic cancellation
viewModelScope.launch {
    // Cancelled when ViewModel is cleared
}

// Proper exception handling
try {
    apiCall()
} catch (e: CancellationException) {
    throw e // Don't catch cancellation
} catch (e: Exception) {
    handleError(e)
}
```

### 5. **Separation of Concerns**

- **Views**: Only UI rendering
- **ViewModels**: State management, no Android dependencies
- **Use Cases**: Business logic, framework-agnostic
- **Repositories**: Data coordination
- **Data Sources**: Specific technology implementations

### 6. **Dependency Inversion**

```kotlin
// High-level modules depend on abstractions
class ViewModel @Inject constructor(
    private val useCase: FeatureUseCase // Not implementation
)

class UseCase @Inject constructor(
    private val repository: Repository // Interface, not Impl
)

// Low-level modules implement abstractions
class RepositoryImpl @Inject constructor(...) : Repository
```

### 7. **Testing Strategy**

```kotlin
// Unit tests with mocks
@Test
fun `loadData should update state to success`() = runTest {
    // Given
    val mockUseCase = mockk<FeatureUseCase>()
    coEvery { mockUseCase() } returns Result.success(testData)

    val viewModel = FeatureViewModel(mockUseCase)

    // When
    viewModel.loadData()

    // Then
    assertEquals(testData, viewModel.uiState.value.data)
    assertEquals(false, viewModel.uiState.value.isLoading)
}
```

### 8. **Code Organization**

- Feature-based package structure
- Clear layer boundaries
- Consistent naming conventions
- Comprehensive documentation

### 9. **Performance Optimization**

```kotlin
// Use remember for expensive operations
val expensiveComputation = remember(key) { compute() }

// LaunchedEffect for side effects
LaunchedEffect(userId) {
    viewModel.loadUser(userId)
}

// StateFlow for state, SharedFlow for events
val uiState: StateFlow<State>       // Hot, cached
val events: SharedFlow<Event>       // Hot, no replay
```

### 10. **Security**

- API keys in `local.properties` (not in VCS)
- BuildConfig for compile-time secrets
- Firebase Security Rules
- Input validation
- HTTPS for all network calls

---

## Architectural Strengths

### ✅ **Testability**

- Each layer can be tested independently
- Easy mocking with interfaces
- ViewModel testing without Android dependencies
- Use case testing in pure Kotlin

### ✅ **Maintainability**

- Clear separation of concerns
- Easy to locate and modify code
- Consistent patterns throughout
- Self-documenting structure

### ✅ **Scalability**

- Easy to add new features
- Modular architecture
- Reusable components
- Clear extension points

### ✅ **Flexibility**

- Easy to swap implementations
- Technology-agnostic domain layer
- Multiple data source support
- Framework changes isolated to data layer

### ✅ **Team Collaboration**

- Clear boundaries between layers
- Feature-based organization
- Consistent coding patterns
- Easy to parallelize development

---

## Potential Improvements

### 1. **Room Database Integration**

Currently planned but not implemented. Would enable:

- Offline-first architecture
- Local caching
- Better performance
- Data persistence

### 2. **Error Tracking**

Integrate Crashlytics or Sentry for:

- Production error monitoring
- Analytics
- User feedback

### 3. **Performance Monitoring**

- Firebase Performance Monitoring
- Network request optimization
- Image loading optimization

### 4. **Advanced Caching**

- Multi-level caching strategy
- Cache expiration policies
- Background sync

### 5. **Modularization**

Split into multiple modules:

- `:app`
- `:feature:chat`
- `:feature:detection`
- `:core:data`
- `:core:domain`
- `:core:ui`

### 6. **CI/CD Pipeline**

- Automated testing
- Code coverage reports
- Automated deployment

---

## Conclusion

GrowCare demonstrates a **production-ready, enterprise-grade architecture** that
follows industry best practices:

- **Clean Architecture** ensures long-term maintainability
- **MVVM with StateFlow** provides reactive, predictable UI
- **Hilt** enables proper dependency management
- **Repository + Use Case patterns** create clear separation
- **Kotlin Coroutines + Flow** handle async operations elegantly
- **Jetpack Compose** delivers modern, declarative UI

The architecture is:

- **Flexible**: Easy to change implementations
- **Testable**: All layers can be tested independently
- **Maintainable**: Clear structure and consistent patterns
- **Scalable**: Easy to add new features
- **Modern**: Uses latest Android development practices

This architecture positions GrowCare for long-term success and easy evolution as
requirements change.

---

**Document Version**: 1.0  
**Created**: December 16, 2025  
**Author**: Architecture Analysis System
