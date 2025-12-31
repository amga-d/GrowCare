package com.example.growCare.domain.usecase.stats

import com.example.growCare.domain.model.ActivityStats
import com.example.growCare.domain.repository.ChatRepository
import com.example.growCare.domain.repository.DetectionRepository
import com.example.growCare.domain.repository.FertilizerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving activity statistics
 */
class GetActivityStatsUseCase @Inject constructor(
    private val detectionRepository: DetectionRepository,
    private val fertilizerRepository: FertilizerRepository,
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<ActivityStats> {
        return combine(
            detectionRepository.getDiseaseHistory(),
            detectionRepository.getSeedHistory(),
            fertilizerRepository.getFertilizerHistory(),
            chatRepository.getAllConversations()
        ) { diseaseHistory, seedHistory, fertilizerHistory, conversations ->
            // Total scans = disease scans + seed scans
            val totalScans = diseaseHistory.size + seedHistory.size
            
            // Total crops = fertilizer calculations (each represents a crop)
            val totalCrops = fertilizerHistory.size
            
            // Total chats = number of conversations
            val totalChats = conversations.size
            
            ActivityStats(
                totalScans = totalScans,
                totalCrops = totalCrops,
                totalChats = totalChats,
                diseaseScans = diseaseHistory.size,
                seedScans = seedHistory.size
            )
        }
    }
}
