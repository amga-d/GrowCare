package com.example.growCare.domain.usecase.detection

import android.net.Uri
import com.example.growCare.domain.model.SeedQuality
import com.example.growCare.domain.repository.DetectionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for analyzing seed quality from images
 */
class AnalyzeSeedQualityUseCase @Inject constructor(
    private val repository: DetectionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        imageUri: Uri,
        seedType: String
    ): Result<SeedQuality> = withContext(dispatcher) {
        try {
            repository.analyzeSeedQuality(imageUri, seedType)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
