package com.example.growCare.domain.usecase.detection

import com.example.growCare.domain.model.SeedQuality
import com.example.growCare.domain.repository.DetectionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for retrieving a specific seed analysis by ID
 */
class GetSeedAnalysisByIdUseCase @Inject constructor(
    private val repository: DetectionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(analysisId: String): Result<SeedQuality?> = withContext(dispatcher) {
        try {
            repository.getSeedAnalysisById(analysisId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
