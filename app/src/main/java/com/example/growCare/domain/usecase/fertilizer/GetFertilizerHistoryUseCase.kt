package com.example.growCare.domain.usecase.fertilizer

import com.example.growCare.domain.model.FertilizerRecommendation
import com.example.growCare.domain.repository.FertilizerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving fertilizer calculation history
 */
class GetFertilizerHistoryUseCase @Inject constructor(
    private val repository: FertilizerRepository
) {
    operator fun invoke(): Flow<List<FertilizerRecommendation>> {
        return repository.getFertilizerHistory()
    }
}
