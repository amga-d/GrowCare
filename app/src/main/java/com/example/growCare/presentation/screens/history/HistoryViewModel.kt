package com.example.growCare.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.growCare.domain.model.DiseaseAnalysis
import com.example.growCare.domain.model.FertilizerRecommendation
import com.example.growCare.domain.model.SeedQuality
import com.example.growCare.domain.usecase.chat.DeleteChatUseCase
import com.example.growCare.domain.usecase.chat.GetAllConversationsUseCase
import com.example.growCare.domain.usecase.detection.DeleteDiseaseScanUseCase
import com.example.growCare.domain.usecase.detection.DeleteSeedScanUseCase
import com.example.growCare.domain.usecase.detection.GetDiseaseAnalysisByIdUseCase
import com.example.growCare.domain.usecase.detection.GetDiseaseHistoryUseCase
import com.example.growCare.domain.usecase.detection.GetSeedAnalysisByIdUseCase
import com.example.growCare.domain.usecase.detection.GetSeedHistoryUseCase
import com.example.growCare.domain.usecase.fertilizer.DeleteFertilizerUseCase
import com.example.growCare.domain.usecase.fertilizer.GetFertilizerByIdUseCase
import com.example.growCare.domain.usecase.fertilizer.GetFertilizerHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getDiseaseHistoryUseCase: GetDiseaseHistoryUseCase,
    private val getSeedHistoryUseCase: GetSeedHistoryUseCase,
    private val getFertilizerHistoryUseCase: GetFertilizerHistoryUseCase,
    private val getAllConversationsUseCase: GetAllConversationsUseCase,
    private val getDiseaseAnalysisByIdUseCase: GetDiseaseAnalysisByIdUseCase,
    private val getSeedAnalysisByIdUseCase: GetSeedAnalysisByIdUseCase,
    private val getFertilizerByIdUseCase: GetFertilizerByIdUseCase,
    private val deleteChatUseCase: DeleteChatUseCase,
    private val deleteDiseaseScanUseCase: DeleteDiseaseScanUseCase,
    private val deleteSeedScanUseCase: DeleteSeedScanUseCase,
    private val deleteFertilizerUseCase: DeleteFertilizerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Combine all history sources
                combine(
                    getDiseaseHistoryUseCase(),
                    getSeedHistoryUseCase(),
                    getFertilizerHistoryUseCase(),
                    getAllConversationsUseCase()
                ) { diseaseHistory, seedHistory, fertilizerHistory, conversations ->
                    
                    val allHistory = buildList {
                        // Add disease history
                        diseaseHistory.forEach { disease ->
                            add(HistoryItem(
                                id = disease.id,
                                type = HistoryType.DISEASE,
                                title = disease.diseaseName ?: "Disease Detection",
                                subtitle = "Confidence: ${disease.confidence}%",
                                timestamp = disease.timestamp,
                                imageUrl = disease.imageUrl
                            ))
                        }

                        // Add seed history
                        seedHistory.forEach { seed ->
                            add(HistoryItem(
                                id = seed.id,
                                type = HistoryType.SEED,
                                title = "Seed Quality Check",
                                subtitle = "Score: ${seed.qualityScore}/100",
                                timestamp = seed.timestamp,
                                imageUrl = seed.imageUrl
                            ))
                        }

                        // Add fertilizer history
                        fertilizerHistory.forEach { fertilizer ->
                            add(HistoryItem(
                                id = fertilizer.id,
                                type = HistoryType.FERTILIZER,
                                title = "${fertilizer.cropType} - ${fertilizer.area} acres",
                                subtitle = "NPK: ${fertilizer.recommendedNPK.toRatioString()}",
                                timestamp = fertilizer.timestamp
                            ))
                        }

                        // Add chat history
                        conversations.forEach { conversation ->
                            add(HistoryItem(
                                id = conversation.id,
                                type = HistoryType.CHAT,
                                title = conversation.title,
                                subtitle = conversation.lastMessage,
                                timestamp = conversation.lastMessageTime
                            ))
                        }
                    }.sortedByDescending { it.timestamp }

                    allHistory
                }.collect { history ->
                    _uiState.update { it.copy(
                        allHistory = history,
                        isLoading = false,
                        error = null
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load history"
                )}
            }
        }
    }

    fun deleteHistoryItem(item: HistoryItem) {
        viewModelScope.launch {
            val result = when (item.type) {
                HistoryType.CHAT -> deleteChatUseCase(item.id)
                HistoryType.DISEASE -> deleteDiseaseScanUseCase(item.id)
                HistoryType.SEED -> deleteSeedScanUseCase(item.id)
                HistoryType.FERTILIZER -> deleteFertilizerUseCase(item.id)
            }

            result.onFailure { error ->
                _uiState.update { it.copy(error = error.message ?: "Failed to delete item") }
            }
            // On success, the flows in loadHistory() will automatically emit new values
        }
    }

    /**
     * Load a specific disease analysis by ID
     * Returns the disease analysis or null if not found
     */
    suspend fun loadDiseaseById(analysisId: String): DiseaseAnalysis? {
        return try {
            _uiState.update { it.copy(isLoading = true) }
            val result = getDiseaseAnalysisByIdUseCase(analysisId)
            _uiState.update { it.copy(isLoading = false) }
            
            if (result.isSuccess) {
                result.getOrNull()
            } else {
                _uiState.update { it.copy(error = "Disease analysis not found") }
                null
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                isLoading = false,
                error = e.message ?: "Failed to load disease analysis"
            )}
            null
        }
    }

    /**
     * Load a specific seed analysis by ID
     * Returns the seed analysis or null if not found
     */
    suspend fun loadSeedById(analysisId: String): SeedQuality? {
        return try {
            _uiState.update { it.copy(isLoading = true) }
            val result = getSeedAnalysisByIdUseCase(analysisId)
            _uiState.update { it.copy(isLoading = false) }
            
            if (result.isSuccess) {
                result.getOrNull()
            } else {
                _uiState.update { it.copy(error = "Seed analysis not found") }
                null
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                isLoading = false,
                error = e.message ?: "Failed to load seed analysis"
            )}
            null
        }
    }

    /**
     * Load a specific fertilizer recommendation by ID
     * Returns the fertilizer recommendation or null if not found
     */
    suspend fun loadFertilizerById(recommendationId: String): FertilizerRecommendation? {
        return try {
            _uiState.update { it.copy(isLoading = true) }
            val result = getFertilizerByIdUseCase(recommendationId)
            _uiState.update { it.copy(isLoading = false) }
            
            if (result.isSuccess) {
                result.getOrNull()
            } else {
                _uiState.update { it.copy(error = "Fertilizer recommendation not found") }
                null
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(
                isLoading = false,
                error = e.message ?: "Failed to load fertilizer recommendation"
            )}
            null
        }
    }
}

data class HistoryUiState(
    val allHistory: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
