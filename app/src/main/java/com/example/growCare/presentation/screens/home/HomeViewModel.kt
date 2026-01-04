package com.example.growCare.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.growCare.domain.model.User
import com.example.growCare.domain.model.WeatherData
import com.example.growCare.domain.repository.AuthRepository
import com.example.growCare.domain.repository.WeatherRepository
import com.example.growCare.domain.usecase.tips.GenerateAITipsUseCase
import com.example.growCare.domain.usecase.user.GetUserProfileUseCase
import com.example.growCare.domain.usecase.user.SaveUserProfileUseCase
import com.example.growCare.data.remote.firebase.FirebaseAuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for HomeScreen
 */
data class HomeUiState(
    val user: User? = null,
    val weather: WeatherData? = null,
    val aiTips: List<Pair<String, String>> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingWeather: Boolean = false,
    val isLoadingTips: Boolean = false,
    val error: String? = null,
    val weatherError: String? = null
)

/**
 * ViewModel for HomeScreen
 * Manages user data, weather data, and AI tips
 * 
 * Follows MVVM architecture:
 * - Uses UseCases for business logic
 * - Manages UI state with StateFlow
 * - No direct access to data sources
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val weatherRepository: WeatherRepository,
    private val generateAITipsUseCase: GenerateAITipsUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val authDataSource: FirebaseAuthDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadWeatherData()
    }

    /**
     * Load weather data and generate AI tips when weather is available
     */
    private fun onWeatherLoaded(weather: WeatherData) {
        _uiState.update {
            it.copy(
                weather = weather,
                isLoadingWeather = false,
                weatherError = null
            )
        }
        // Generate AI tips based on weather
        generateAITips(weather)
    }

    /**
     * Load current user data from repository (observes real-time changes)
     */
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Observe user profile from Firestore/Room for real-time updates
                getUserProfileUseCase().collect { user ->
                    if (user == null) {
                        // Profile doesn't exist, create it from Firebase Auth
                        createProfileFromAuth()
                    } else {
                        _uiState.update {
                            it.copy(
                                user = user,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun createProfileFromAuth() {
        viewModelScope.launch {
            try {
                val firebaseUser = authDataSource.getCurrentUser()
                if (firebaseUser != null) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore('@'),
                        phoneNumber = firebaseUser.phoneNumber,
                        location = null,
                        farmSize = null,
                        profilePictureUrl = firebaseUser.photoUrl?.toString(),
                        preferredCrops = emptyList(),
                        createdAt = System.currentTimeMillis(),
                        lastLoginAt = System.currentTimeMillis()
                    )
                    
                    saveUserProfileUseCase(newUser)
                        .onSuccess {
                            // The collect above will pick up the new profile
                            _uiState.update { it.copy(isLoading = false) }
                        }
                        .onFailure {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Load weather data for user's location
     */
    private fun loadWeatherData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWeather = true) }

            weatherRepository.getCurrentWeather()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingWeather = false,
                            weatherError = e.message ?: "Failed to load weather"
                        )
                    }
                }
                .collect { weatherData ->
                    onWeatherLoaded(weatherData)
                }
        }
    }

    /**
     * Refresh user data
     */
    fun refreshUserData() {
        loadUserData()
    }

    /**
     * Refresh weather data
     */
    fun refreshWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWeather = true) }
            weatherRepository.refreshWeather()
                .onSuccess {
                    _uiState.update { it.copy(isLoadingWeather = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingWeather = false,
                            weatherError = e.message
                        )
                    }
                }
        }
    }

    /**
     * Handle location permission result
     */
    fun onLocationPermissionResult(granted: Boolean) {
        if (granted) {
            // Permission granted, reload weather data
            loadWeatherData()
        } else {
            // Permission denied, show error
            _uiState.update {
                it.copy(
                    isLoadingWeather = false,
                    weatherError = "Location permission required for weather data"
                )
            }
        }
    }

    /**
     * Generate AI-powered agricultural tips using UseCase
     * Follows Clean Architecture - ViewModel calls UseCase, not Repository
     */
    private fun generateAITips(weather: WeatherData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTips = true) }
            
            try {
                generateAITipsUseCase(weather).collect { tips ->
                    _uiState.update {
                        it.copy(
                            aiTips = tips,
                            isLoadingTips = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        aiTips = emptyList(),
                        isLoadingTips = false
                    )
                }
            }
        }
    }
}
