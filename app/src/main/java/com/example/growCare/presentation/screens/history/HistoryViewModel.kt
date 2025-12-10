package com.example.growCare.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.growCare.domain.usecase.chat.GetChatHistoryUseCase
import com.example.growCare.domain.usecase.detection.GetDiseaseHistoryUseCase
import com.example.growCare.domain.usecase.detection.GetSeedHistoryUseCase
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
    private val getChatHistoryUseCase: GetChatHistoryUseCase
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
                    getChatHistoryUseCase("all") // Get all chat sessions
                ) { diseaseHistory, seedHistory, fertilizerHistory, chatHistory ->
                    
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

                        // Add chat history (group by conversation)
                        chatHistory.groupBy { it.conversationId }.forEach { (conversationId, messages) ->
                            val firstMessage = messages.firstOrNull()
                            if (firstMessage != null) {
                                add(HistoryItem(
                                    id = conversationId ?: "chat_${System.currentTimeMillis()}",
                                    type = HistoryType.CHAT,
                                    title = firstMessage.content.take(30) + if (firstMessage.content.length > 30) "..." else "",
                                    subtitle = "${messages.size} messages",
                                    timestamp = firstMessage.timestamp
                                ))
                            }
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
}

data class HistoryUiState(
    val allHistory: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
