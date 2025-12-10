package com.example.growCare.domain.usecase.detection

import com.example.growCare.domain.model.SeedQuality
import com.example.growCare.domain.repository.DetectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving seed quality analysis history
 */
class GetSeedHistoryUseCase @Inject constructor(
    private val repository: DetectionRepository
) {
    operator fun invoke(): Flow<List<SeedQuality>> {
        return repository.getSeedHistory()
    }
}
