package com.example.growCare.domain.usecase.fertilizer

import com.example.growCare.domain.repository.FertilizerRepository
import javax.inject.Inject

class DeleteFertilizerUseCase @Inject constructor(
    private val repository: FertilizerRepository
) {
    suspend operator fun invoke(recommendationId: String): Result<Unit> {
        return repository.deleteFertilizerRecommendation(recommendationId)
    }
}
