package com.example.growCare.domain.usecase.fertilizer

import com.example.growCare.domain.model.FertilizerRecommendation
import com.example.growCare.domain.model.NPK
import com.example.growCare.domain.repository.FertilizerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for calculating fertilizer recommendations
 */
class CalculateFertilizerUseCase @Inject constructor(
    private val repository: FertilizerRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        cropType: String,
        soilType: String,
        area: Double,
        currentNPK: NPK,
        targetYield: Double? = null,
        growthStage: String? = null
    ): Result<FertilizerRecommendation> = withContext(dispatcher) {
        try {
            repository.calculateFertilizer(
                cropType = cropType,
                soilType = soilType,
                area = area,
                currentNPK = currentNPK,
                targetYield = targetYield,
                growthStage = growthStage
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend operator fun invoke(
        query: String
    ): Result<FertilizerRecommendation> = withContext(dispatcher) {
        try {
            repository.calculateFertilizerFromText(query)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
