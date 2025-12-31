package com.example.growCare.domain.usecase.fertilizer

import com.example.growCare.domain.model.FertilizerRecommendation
import com.example.growCare.domain.repository.FertilizerRepository
import javax.inject.Inject

/**
 * Use case for retrieving a specific fertilizer recommendation by ID
 */
class GetFertilizerByIdUseCase @Inject constructor(
    private val repository: FertilizerRepository
) {
    suspend operator fun invoke(recommendationId: String): Result<FertilizerRecommendation?> {
        return repository.getFertilizerById(recommendationId)
    }
}
