package com.example.growCare.presentation.screens.fertilizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.growCare.domain.model.FertilizerRecommendation
import com.example.growCare.domain.model.NPK
import com.example.growCare.domain.model.SoilType
import com.example.growCare.domain.usecase.fertilizer.CalculateFertilizerUseCase
import com.example.growCare.domain.usecase.fertilizer.GetFertilizerHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Fertilizer Calculator feature
 * Handles form validation, NPK calculation, and result navigation
 */
@HiltViewModel
class FertilizerViewModel @Inject constructor(
    private val calculateFertilizerUseCase: CalculateFertilizerUseCase,
    private val getFertilizerHistoryUseCase: GetFertilizerHistoryUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(FertilizerUiState())
    val uiState: StateFlow<FertilizerUiState> = _uiState.asStateFlow()

    // Events for one-time actions
    private val _events = MutableSharedFlow<FertilizerEvent>()
    val events: SharedFlow<FertilizerEvent> = _events.asSharedFlow()

    /**
     * Handle user actions
     */
    fun onAction(action: FertilizerAction) {
        when (action) {
            is FertilizerAction.UpdateCropType -> updateCropType(action.cropType)
            is FertilizerAction.UpdateGrowthStage -> updateGrowthStage(action.stage)
            is FertilizerAction.UpdateSoilType -> updateSoilType(action.soilType)
            is FertilizerAction.UpdateAreaSize -> updateAreaSize(action.area)
            is FertilizerAction.UpdateTargetYield -> updateTargetYield(action.yield)
            is FertilizerAction.UpdateCurrentNPK -> updateCurrentNPK(action.npk)
            FertilizerAction.Calculate -> calculateFertilizer()
            FertilizerAction.Reset -> resetForm()
            FertilizerAction.LoadHistory -> loadHistory()
        }
    }

    /**
     * Update crop type and validate
     */
    private fun updateCropType(cropType: String) {
        _uiState.update { it.copy(
            cropType = cropType,
            cropTypeError = if (cropType.isBlank()) "Crop type is required" else null
        )}
    }

    /**
     * Update growth stage
     */
    private fun updateGrowthStage(stage: String) {
        _uiState.update { it.copy(growthStage = stage) }
    }

    /**
     * Update soil type and validate
     */
    private fun updateSoilType(soilType: String) {
        _uiState.update { it.copy(
            soilType = soilType,
            soilTypeError = if (soilType.isBlank()) "Soil type is required" else null
        )}
    }

    /**
     * Update area size and validate
     */
    private fun updateAreaSize(area: String) {
        val areaValue = area.toDoubleOrNull()
        _uiState.update { it.copy(
            areaSize = area,
            areaSizeError = when {
                area.isBlank() -> "Area is required"
                areaValue == null -> "Enter a valid number"
                areaValue <= 0 -> "Area must be greater than 0"
                else -> null
            }
        )}
    }

    /**
     * Update target yield
     */
    private fun updateTargetYield(yieldValue: Float) {
        _uiState.update { it.copy(
            targetYield = yieldValue,
            targetYieldError = if (yieldValue <= 0) "Yield must be greater than 0" else null
        )}
    }

    /**
     * Update current NPK values (for manual input tab)
     */
    private fun updateCurrentNPK(npk: NPK) {
        _uiState.update { it.copy(
            currentNPK = npk,
            currentNPKError = when {
                npk.nitrogen < 0 || npk.phosphorus < 0 || npk.potassium < 0 -> 
                    "NPK values must be non-negative"
                else -> null
            }
        )}
    }

    /**
     * Validate all form fields
     */
    private fun validateForm(): Boolean {
        val state = _uiState.value
        
        // Update errors
        _uiState.update { it.copy(
            cropTypeError = if (state.cropType.isBlank()) "Crop type is required" else null,
            soilTypeError = if (state.soilType.isBlank()) "Soil type is required" else null,
            areaSizeError = when {
                state.areaSize.isBlank() -> "Area is required"
                state.areaSize.toDoubleOrNull() == null -> "Enter a valid number"
                state.areaSize.toDoubleOrNull()!! <= 0 -> "Area must be greater than 0"
                else -> null
            },
            targetYieldError = if (state.targetYield <= 0) "Yield must be greater than 0" else null
        )}

        // Check if any errors exist
        val hasErrors = _uiState.value.let {
            it.cropTypeError != null ||
            it.soilTypeError != null ||
            it.areaSizeError != null ||
            it.targetYieldError != null ||
            it.currentNPKError != null
        }

        return !hasErrors
    }

    /**
     * Calculate fertilizer recommendation
     */
    private fun calculateFertilizer() {
        if (!validateForm()) {
            viewModelScope.launch {
                _events.emit(FertilizerEvent.ShowError("Please fix form errors"))
            }
            return
        }

        val state = _uiState.value
        val areaValue = state.areaSize.toDoubleOrNull() ?: return

        _uiState.update { it.copy(
            isCalculating = true,
            error = null
        )}

        viewModelScope.launch {
            try {
                // Convert soil type string to enum
                val soilTypeEnum = when (state.soilType.lowercase()) {
                    "loamy" -> SoilType.LOAMY
                    "sandy" -> SoilType.SANDY
                    "clay" -> SoilType.CLAY
                    "silty" -> SoilType.SILTY
                    "peaty" -> SoilType.PEATY
                    "chalky" -> SoilType.CHALKY
                    else -> SoilType.LOAMY
                }

                calculateFertilizerUseCase(
                    cropType = state.cropType,
                    soilType = soilTypeEnum.name,
                    area = areaValue,
                    currentNPK = state.currentNPK,
                    targetYield = state.targetYield.toDouble()
                )
                    .onSuccess { recommendation ->
                        _uiState.update { it.copy(
                            isCalculating = false,
                            result = recommendation,
                            error = null
                        )}
                        _events.emit(FertilizerEvent.NavigateToResult(recommendation))
                    }
                    .onFailure { error ->
                        _uiState.update { it.copy(
                            isCalculating = false,
                            error = error.message ?: "Calculation failed"
                        )}
                        _events.emit(FertilizerEvent.ShowError(error.message ?: "Calculation failed"))
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isCalculating = false,
                    error = e.message ?: "An error occurred"
                )}
                _events.emit(FertilizerEvent.ShowError(e.message ?: "An error occurred"))
            }
        }
    }

    /**
     * Reset form to initial state
     */
    private fun resetForm() {
        _uiState.value = FertilizerUiState()
    }

    /**
     * Load calculation history
     */
    private fun loadHistory() {
        viewModelScope.launch {
            getFertilizerHistoryUseCase().collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }
}

/**
 * UI State for Fertilizer screen
 */
data class FertilizerUiState(
    // Form fields
    val cropType: String = "",
    val growthStage: String = "Vegetative",
    val soilType: String = "",
    val areaSize: String = "",
    val targetYield: Float = 180f,
    val currentNPK: NPK = NPK(0.0, 0.0, 0.0),
    
    // Validation errors
    val cropTypeError: String? = null,
    val soilTypeError: String? = null,
    val areaSizeError: String? = null,
    val targetYieldError: String? = null,
    val currentNPKError: String? = null,
    
    // State
    val isCalculating: Boolean = false,
    val error: String? = null,
    val result: FertilizerRecommendation? = null,
    val history: List<FertilizerRecommendation> = emptyList()
)

/**
 * User actions in Fertilizer screen
 */
sealed interface FertilizerAction {
    data class UpdateCropType(val cropType: String) : FertilizerAction
    data class UpdateGrowthStage(val stage: String) : FertilizerAction
    data class UpdateSoilType(val soilType: String) : FertilizerAction
    data class UpdateAreaSize(val area: String) : FertilizerAction
    data class UpdateTargetYield(val yield: Float) : FertilizerAction
    data class UpdateCurrentNPK(val npk: NPK) : FertilizerAction
    data object Calculate : FertilizerAction
    data object Reset : FertilizerAction
    data object LoadHistory : FertilizerAction
}

/**
 * One-time events from ViewModel
 */
sealed interface FertilizerEvent {
    data class NavigateToResult(val recommendation: FertilizerRecommendation) : FertilizerEvent
    data class ShowError(val message: String) : FertilizerEvent
    data class ShowMessage(val message: String) : FertilizerEvent
}
