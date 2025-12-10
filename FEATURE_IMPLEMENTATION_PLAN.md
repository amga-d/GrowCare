# GrowCare - Feature Implementation Plan

**Date**: December 10, 2025  
**Status**: In Progress  
**Target Completion**: December 31, 2025

---

## 🎯 Overview

Implementation plan for 4 core AI-powered features connecting existing UI and
Firebase/Gemini infrastructure through Clean Architecture layers (Repository →
UseCase → ViewModel → UI).

### Current Status: 27.5% Complete

- ✅ Infrastructure: 95% (Gemini AI, Firebase, Hilt, Navigation)
- ✅ UI Layer: 85% (Screens designed, waiting for backend)
- ⚠️ Data Layer: 60% (Interfaces ready, implementations missing)
- ❌ Domain Layer: 0% (No UseCases exist)

---

## 🚀 Implementation Phases

### Phase 1: Foundation Layer (Days 1-3)

**Goal**: Connect all layers of Clean Architecture

#### 1.1 Repository Implementations

- [ ] `DetectionRepositoryImpl.kt` - Disease & Seed analysis
- [ ] `ChatRepositoryImpl.kt` - AI chat with history
- [ ] `FertilizerRepositoryImpl.kt` - NPK calculations
- [ ] Update `RepositoryModule.kt` - Bind all repositories

#### 1.2 Use Cases (Business Logic)

- [ ] `AnalyzePlantDiseaseUseCase.kt`
- [ ] `AnalyzeSeedQualityUseCase.kt`
- [ ] `SendChatMessageUseCase.kt`
- [ ] `GetChatHistoryUseCase.kt`
- [ ] `CalculateFertilizerUseCase.kt`
- [ ] `GetDetectionHistoryUseCase.kt`
- [ ] `GetFertilizerHistoryUseCase.kt`

---

### Phase 2: Feature Implementation (Days 4-8)

#### 2.1 Chat Support (Priority 1 - Easiest)

**Why First**: UI 85% done, streaming already implemented in Gemini client

- [ ] `ChatViewModel.kt` - State management with StateFlow
  - Message list state
  - Streaming response handling
  - Loading/error states
  - Event handling (scroll to bottom, show errors)
- [ ] Connect `ChatScreen.kt` to ViewModel
  - Replace local state with ViewModel state
  - Implement send message flow
  - Add streaming UI updates
- [ ] Firebase persistence
  - Save messages to Firestore
  - Load chat history on screen open
- [ ] Image attachment support
  - Connect camera button
  - Upload to Storage
  - Send with Gemini Vision API

**Estimated**: 1 day

---

#### 2.2 Fertilizer Recipe (Priority 2 - UI Complete)

**Why Second**: UI 95% complete, clear inputs/outputs

- [ ] `FertilizerViewModel.kt` - Form handling
  - Input validation
  - Loading state during calculation
  - Result state management
  - Share functionality
- [ ] Result Screen/Dialog
  - Display NPK values
  - Show application schedule
  - Product recommendations
  - Cost estimation
- [ ] AI Response Parser
  - Parse Gemini JSON response
  - Map to `FertilizerRecommendation` model
  - Handle parsing errors gracefully
- [ ] History tracking
  - Save calculations to Firestore
  - Load previous calculations

**Estimated**: 1.5 days

---

#### 2.3 Plant Disease Detection (Priority 3 - Needs Camera)

**Why Third**: Requires camera implementation, complex parsing

- [ ] Camera Implementation
  - `CameraPreview.kt` composable
  - `ImageCapture.kt` utility
  - Permission handling
  - Gallery picker option
- [ ] `DiseaseViewModel.kt` - Image analysis
  - Image capture state
  - Upload to Firebase Storage
  - Call Gemini Vision API
  - Parse disease analysis response
  - Error handling for API failures
- [ ] Update `DiseaseScanScreen.kt`
  - Integrate camera preview
  - Show capture button
  - Loading overlay during analysis
  - Navigate to result on success
- [ ] `DiseaseResultScreen.kt` - Display results
  - Disease name and confidence
  - Symptoms list
  - Treatment recommendations
  - Prevention measures
  - Save to history option
- [ ] AI Response Parser
  - Parse structured disease analysis
  - Validate confidence scores
  - Handle "no disease" cases

**Estimated**: 2 days

---

#### 2.4 Seed Quality Assessment (Priority 4 - Similar to Disease)

**Why Last**: Can reuse camera implementation

- [ ] `SeedViewModel.kt` - Quality analysis
  - Reuse camera components
  - Upload to Firebase Storage
  - Call Gemini Vision API
  - Parse seed quality response
- [ ] Update `SeedScanScreen.kt`
  - Add camera preview
  - Integrate with ViewModel
- [ ] `SeedResultScreen.kt` - Quality report
  - Quality score gauge
  - Size assessment
  - Color consistency
  - Damage report
  - Germination potential
  - Recommendations
- [ ] AI Response Parser
  - Parse seed quality metrics
  - Handle edge cases

**Estimated**: 1.5 days

---

### Phase 3: Polish & Testing (Days 9-10)

#### 3.1 Error Handling & UX

- [ ] Comprehensive error messages
- [ ] Retry mechanisms
- [ ] Loading states with progress indicators
- [ ] Empty states (no history, etc.)
- [ ] Success animations
- [ ] Toast notifications

#### 3.2 History Screens

- [ ] Disease detection history list
- [ ] Seed quality history list
- [ ] Fertilizer calculation history
- [ ] Detail views for historical items
- [ ] Delete functionality

#### 3.3 Testing

- [ ] Unit tests for UseCases
- [ ] Unit tests for Repository implementations
- [ ] ViewModel tests
- [ ] Integration tests for critical flows
- [ ] Manual testing on physical device

**Estimated**: 2 days

---

## 📋 Detailed Task Breakdown

### Task 1: Repository Implementations

#### DetectionRepositoryImpl.kt

```kotlin
Location: data/repository/DetectionRepositoryImpl.kt
Dependencies: GeminiClient, FirestoreDataSource, FirebaseStorageDataSource
Methods to implement:
  - analyzePlantDisease(imageUri, userId) -> Result<DiseaseAnalysis>
  - analyzeSeedQuality(imageUri, userId) -> Result<SeedQuality>
  - getDiseaseHistory(userId) -> Flow<List<DiseaseAnalysis>>
  - getSeedHistory(userId) -> Flow<List<SeedQuality>>
  - saveDiseaseAnalysis() -> Result<Unit>
  - saveSeedAnalysis() -> Result<Unit>
```

#### ChatRepositoryImpl.kt

```kotlin
Location: data/repository/ChatRepositoryImpl.kt
Dependencies: GeminiClient, FirestoreDataSource
Methods to implement:
  - sendMessage(message, conversationId) -> Flow<ChatMessage>
  - sendMessageWithImage(message, imageUri, conversationId) -> Flow<ChatMessage>
  - getChatHistory(conversationId) -> Flow<List<ChatMessage>>
  - getAllConversations(userId) -> Flow<List<Conversation>>
  - deleteMessage(messageId) -> Result<Unit>
```

#### FertilizerRepositoryImpl.kt

```kotlin
Location: data/repository/FertilizerRepositoryImpl.kt
Dependencies: GeminiClient, FirestoreDataSource
Methods to implement:
  - calculateFertilizer(input) -> Result<FertilizerRecommendation>
  - getFertilizerHistory(userId) -> Flow<List<FertilizerRecommendation>>
  - saveFertilizerCalculation() -> Result<Unit>
  - getCropNPKRequirements(cropType) -> NPK
  - getOrganicAlternatives(npk) -> List<String>
```

---

### Task 2: Use Cases

#### Pattern for All UseCases:

```kotlin
class FeatureUseCase @Inject constructor(
    private val repository: FeatureRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(params: Params): Result<Data> = withContext(dispatcher) {
        try {
            repository.performAction(params)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### List of UseCases to Create:

1. **Detection UseCases** (domain/usecase/detection/)

   - AnalyzePlantDiseaseUseCase
   - AnalyzeSeedQualityUseCase
   - GetDiseaseHistoryUseCase
   - GetSeedHistoryUseCase

2. **Chat UseCases** (domain/usecase/chat/)

   - SendChatMessageUseCase (with streaming)
   - GetChatHistoryUseCase
   - SendMessageWithImageUseCase

3. **Fertilizer UseCases** (domain/usecase/fertilizer/)
   - CalculateFertilizerUseCase
   - GetFertilizerHistoryUseCase

---

### Task 3: ViewModel Implementations

#### Standard ViewModel Pattern:

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val useCase: FeatureUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeatureUiState())
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeatureEvent>()
    val events: SharedFlow<FeatureEvent> = _events.asSharedFlow()

    fun onAction(action: FeatureAction) {
        viewModelScope.launch {
            when (action) {
                is FeatureAction.LoadData -> loadData()
                is FeatureAction.Submit -> submitData(action.data)
            }
        }
    }
}
```

#### ViewModels to Implement:

1. **ChatViewModel** - Message streaming, history, image attachments
2. **FertilizerViewModel** - Form validation, calculation, results
3. **DiseaseViewModel** - Image capture, analysis, results
4. **SeedViewModel** - Image capture, quality analysis, results

---

### Task 4: Camera Integration

#### Components to Create:

```kotlin
presentation/components/
  ├── CameraPreview.kt - Preview composable with CameraX
  ├── ImageCapture.kt - Capture utility functions
  └── ImagePicker.kt - Gallery + Camera option dialog
```

#### Features:

- Camera permission handling
- Preview with overlay
- Capture button
- Gallery selection fallback
- Image compression before upload
- Error handling

---

### Task 5: AI Response Parsers

#### Challenges:

- Gemini returns plain text or markdown
- Need to extract structured data
- Handle variations in format
- Graceful error recovery

#### Implementation Strategy:

```kotlin
// In Repository implementations
private fun parseDiseaseResponse(response: String): DiseaseAnalysis {
    return try {
        // Try JSON parsing first
        Json.decodeFromString<DiseaseAnalysis>(response)
    } catch (e: Exception) {
        // Fallback: Regex extraction from markdown
        extractDiseaseFromMarkdown(response)
    }
}
```

---

## 🔄 Data Flow Architecture

### Complete Flow Example (Disease Detection):

```
User taps "Capture" in DiseaseScanScreen
    ↓
DiseaseScanScreen calls viewModel.captureImage(uri)
    ↓
DiseaseViewModel.onAction(DiseaseAction.AnalyzeImage(uri))
    ↓ (viewModelScope.launch)
AnalyzePlantDiseaseUseCase.invoke(uri, userId)
    ↓ (withContext(Dispatchers.IO))
DetectionRepository.analyzePlantDisease(uri, userId)
    ↓
DetectionRepositoryImpl performs:
    1. Upload image to Firebase Storage
    2. Call GeminiClient.analyzePlantDisease(uri)
    3. Parse response to DiseaseAnalysis
    4. Save to Firestore
    5. Return Result<DiseaseAnalysis>
    ↓
UseCase returns Result to ViewModel
    ↓
ViewModel updates _uiState with result
    ↓
DiseaseScanScreen collects uiState
    ↓
UI navigates to DiseaseResultScreen with data
```

---

## 🧪 Testing Strategy

### Unit Tests Priority:

1. **UseCases** (Critical)

   - Test business logic
   - Mock repositories
   - Verify error handling

2. **Repository Implementations** (High)

   - Test data transformations
   - Mock data sources
   - Test offline scenarios

3. **ViewModels** (High)

   - Test state updates
   - Mock use cases
   - Test event emission

4. **Parsers** (Medium)
   - Test various AI response formats
   - Edge cases
   - Malformed data

### Integration Tests:

- End-to-end flow for each feature
- Firebase emulator for Firestore tests
- Mock Gemini API responses

---

## 📊 Success Metrics

### Technical Metrics:

- [ ] All 4 features functional end-to-end
- [ ] No memory leaks in ViewModels
- [ ] Proper error handling with user feedback
- [ ] < 3s response time for AI queries
- [ ] Offline support for viewing history
- [ ] 80%+ code coverage for business logic

### User Experience:

- [ ] Smooth camera preview (30+ fps)
- [ ] Loading states during AI processing
- [ ] Clear error messages
- [ ] Intuitive result displays
- [ ] Quick access to history

---

## 🚨 Known Challenges & Solutions

### Challenge 1: AI Response Inconsistency

**Problem**: Gemini may format responses differently  
**Solution**: Robust parsing with multiple strategies (JSON → Regex → Fallback)

### Challenge 2: Image Upload Performance

**Problem**: Large images slow upload  
**Solution**: Compress images to 1024x1024 before upload

### Challenge 3: Offline Functionality

**Problem**: Features won't work without internet  
**Solution**: Phase 4 - Add Room database caching

### Challenge 4: Camera Permissions

**Problem**: Users may deny permissions  
**Solution**: Clear permission rationale, gallery fallback option

### Challenge 5: Cost of Gemini API

**Problem**: Many API calls can be expensive  
**Solution**: Cache results, rate limiting, efficient prompts

---

## 📅 Timeline

| Phase                 | Duration | Completion Date |
| --------------------- | -------- | --------------- |
| Phase 1: Foundation   | 3 days   | Dec 13, 2025    |
| Phase 2.1: Chat       | 1 day    | Dec 14, 2025    |
| Phase 2.2: Fertilizer | 1.5 days | Dec 16, 2025    |
| Phase 2.3: Disease    | 2 days   | Dec 18, 2025    |
| Phase 2.4: Seed       | 1.5 days | Dec 20, 2025    |
| Phase 3: Polish       | 2 days   | Dec 22, 2025    |
| **Buffer**            | 9 days   | Dec 31, 2025    |

---

## 🔧 Development Workflow

### Daily Process:

1. Morning: Review plan, pick task
2. Implementation with TDD approach
3. Test on emulator
4. Commit with descriptive message
5. Update this document

### Commit Message Format:

```
feat(detection): Implement plant disease analysis repository
fix(chat): Resolve message duplication in streaming
refactor(fertilizer): Extract calculation logic to UseCase
test(detection): Add unit tests for disease parsing
docs(plan): Update completion status
```

---

## 📝 Notes

### Architecture Decisions:

- **UseCases**: Single Responsibility Principle - One use case per action
- **State Management**: StateFlow for UI state, SharedFlow for one-time events
- **Error Handling**: Result<T> pattern for operations that can fail
- **Coroutines**: IO dispatcher for network/disk operations
- **Image Handling**: Compress before upload, cache in app directory

### Future Enhancements (Post-Launch):

- [ ] Room database for offline support
- [ ] Multi-language support
- [ ] Voice input for chat
- [ ] Crop health monitoring dashboard
- [ ] Community sharing of disease reports
- [ ] Fertilizer cost tracking over time
- [ ] Integration with weather forecasts
- [ ] Push notifications for crop alerts

---

## 🎯 Current Status

### ✅ Phase 1 Progress: Foundation Layer (90% Complete)

**Completed:**

- ✅ DetectionRepositoryImpl.kt created (548 lines) with AI response parsing
- ✅ ChatRepositoryImpl.kt created (261 lines) with streaming support
- ✅ FertilizerRepositoryImpl.kt created (417 lines) with NPK calculations
- ✅ RepositoryModule.kt updated with all 3 repository bindings
- ✅ Created 9 UseCases in proper directory structure:
  - Detection: AnalyzePlantDiseaseUseCase, AnalyzeSeedQualityUseCase,
    GetDiseaseHistoryUseCase, GetSeedHistoryUseCase
  - Chat: SendChatMessageUseCase, SendMessageWithImageUseCase,
    GetChatHistoryUseCase
  - Fertilizer: CalculateFertilizerUseCase, GetFertilizerHistoryUseCase

**Remaining Issues (10%):**

- ⚠️ Minor compilation errors due to mismatched method signatures
- ⚠️ SeedQuality model has `sizeAssessment` field but code uses `size`
- ⚠️ GeminiClient.getFertilizerRecommendation expects String but receives NPK
  object
- ⚠️ Some FirestoreDataSource methods missing (will create stub methods)
- ⚠️ ChatRepository needs history conversion for Gemini streaming

**Next Steps:**

1. Fix compilation errors (parameter name mismatches, add missing Firestore
   methods)
2. Test repository layer with unit tests
3. Move to Phase 2: Implement ViewModels starting with ChatViewModel

---

**Last Updated**: December 10, 2025 18:45  
**Status**: Phase 1 near completion, fixing compilation issues
