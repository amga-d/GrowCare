package com.example.growCare.presentation.screens.seed

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.growCare.domain.model.SeedQuality
import com.example.growCare.domain.usecase.detection.AnalyzeSeedQualityUseCase
import com.example.growCare.domain.usecase.detection.GetSeedHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Seed Quality Assessment feature
 * Handles image capture, AI analysis, and quality scoring
 */
@HiltViewModel
class SeedViewModel @Inject constructor(
    private val analyzeSeedQualityUseCase: AnalyzeSeedQualityUseCase,
    private val getSeedHistoryUseCase: GetSeedHistoryUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(SeedUiState())
    val uiState: StateFlow<SeedUiState> = _uiState.asStateFlow()

    // Events for one-time actions
    private val _events = MutableSharedFlow<SeedEvent>()
    val events: SharedFlow<SeedEvent> = _events.asSharedFlow()

    init {
        loadHistory()
    }

    /**
     * Handle user actions
     */
    fun onAction(action: SeedAction) {
        when (action) {
            is SeedAction.CaptureImage -> handleImageCapture(action.uri)
            is SeedAction.AnalyzeImage -> analyzeImage(action.uri)
            SeedAction.RetryAnalysis -> retryAnalysis()
            SeedAction.ClearResult -> clearResult()
            SeedAction.ShowCamera -> showCamera()
            SeedAction.HideCamera -> hideCamera()
        }
    }

    /**
     * Show camera for image capture
     */
    private fun showCamera() {
        _uiState.update { it.copy(
            showCamera = true,
            error = null
        )}
    }

    /**
     * Hide camera
     */
    private fun hideCamera() {
        _uiState.update { it.copy(showCamera = false) }
    }

    /**
     * Handle captured image
     */
    private fun handleImageCapture(uri: Uri) {
        _uiState.update { it.copy(
            capturedImageUri = uri,
            showCamera = false,
            error = null
        )}
        // Automatically start analysis
        analyzeImage(uri)
    }

    /**
     * Analyze seed quality from image
     */
    private fun analyzeImage(uri: Uri) {
        _uiState.update { it.copy(
            isAnalyzing = true,
            error = null,
            result = null
        )}

        viewModelScope.launch {
            try {
                // For now, use "Unknown" as default seed type
                // In a real app, this could be user-selected or detected by AI
                analyzeSeedQualityUseCase(uri, seedType = "Unknown")
                    .onSuccess { analysis ->
                        _uiState.update { it.copy(
                            isAnalyzing = false,
                            result = analysis,
                            error = null
                        )}
                        _events.emit(SeedEvent.AnalysisComplete(analysis, uri.toString()))
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(
                            isAnalyzing = false,
                            error = error.message ?: "Analysis failed"
                        )}
                        _events.emit(SeedEvent.ShowError(error.message ?: "Analysis failed"))
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isAnalyzing = false,
                    error = e.message ?: "An error occurred"
                )}
                _events.emit(SeedEvent.ShowError(e.message ?: "An error occurred"))
            }
        }
    }

    /**
     * Retry last analysis
     */
    private fun retryAnalysis() {
        _uiState.value.capturedImageUri?.let { uri ->
            analyzeImage(uri)
        }
    }

    /**
     * Clear result and reset for new capture
     */
    private fun clearResult() {
        _uiState.update { SeedUiState() }
    }

    /**
     * Load analysis history
     */
    private fun loadHistory() {
        viewModelScope.launch {
            getSeedHistoryUseCase().collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    /**
     * Get quality color based on score
     */
    fun getQualityColor(score: Int): androidx.compose.ui.graphics.Color {
        return when {
            score >= 80 -> com.example.growCare.ui.theme.SuccessGreen
            score >= 60 -> com.example.growCare.ui.theme.SecondaryAmber
            else -> com.example.growCare.ui.theme.DiseaseRed
        }
    }

    /**
     * Get quality label based on score
     */
    fun getQualityLabel(score: Int): String {
        return when {
            score >= 90 -> "Excellent"
            score >= 80 -> "Very Good"
            score >= 70 -> "Good"
            score >= 60 -> "Fair"
            score >= 50 -> "Poor"
            else -> "Very Poor"
        }
    }
}

/**
 * UI State for Seed Quality screen
 */
data class SeedUiState(
    val capturedImageUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val result: SeedQuality? = null,
    val error: String? = null,
    val showCamera: Boolean = false,
    val history: List<SeedQuality> = emptyList()
)

/**
 * User actions in Seed Quality screen
 */
sealed interface SeedAction {
    data class CaptureImage(val uri: Uri) : SeedAction
    data class AnalyzeImage(val uri: Uri) : SeedAction
    data object RetryAnalysis : SeedAction
    data object ClearResult : SeedAction
    data object ShowCamera : SeedAction
    data object HideCamera : SeedAction
}

/**
 * One-time events from ViewModel
 */
sealed interface SeedEvent {
    data class AnalysisComplete(val analysis: SeedQuality, val imageUrl: String) : SeedEvent
    data class ShowError(val message: String) : SeedEvent
    data class ShowMessage(val message: String) : SeedEvent
}
