package com.example.growCare.domain.repository

import com.example.growCare.domain.model.CropData
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for crop management operations
 * Handles CRUD operations for crop data
 */
interface CropRepository {
    
    /**
     * Get all crops for the current user
     * @return Flow of crop list, updates in real-time from Firestore
     */
    fun getAllCrops(): Flow<List<CropData>>
    
    /**
     * Get a specific crop by ID
     */
    suspend fun getCropById(cropId: String): Result<CropData?>
    
    /**
     * Add a new crop
     */
    suspend fun addCrop(crop: CropData): Result<Unit>
    
    /**
     * Update existing crop data
     */
    suspend fun updateCrop(crop: CropData): Result<Unit>
    
    /**
     * Delete a crop
     */
    suspend fun deleteCrop(cropId: String): Result<Unit>
    
    /**
     * Get crops by health status
     */
    fun getCropsByHealthStatus(status: com.example.growCare.domain.model.HealthStatus): Flow<List<CropData>>
    
    /**
     * Get crops by stage
     */
    fun getCropsByStage(stage: com.example.growCare.domain.model.CropStage): Flow<List<CropData>>
    
    /**
     * Search crops by name
     */
    fun searchCrops(query: String): Flow<List<CropData>>
    
    /**
     * Get crops ready for harvest (within next 7 days)
     */
    fun getCropsReadyForHarvest(): Flow<List<CropData>>
    
    /**
     * Update crop health status
     */
    suspend fun updateCropHealth(
        cropId: String,
        healthStatus: com.example.growCare.domain.model.HealthStatus
    ): Result<Unit>
    
    /**
     * Update crop stage
     */
    suspend fun updateCropStage(
        cropId: String,
        stage: com.example.growCare.domain.model.CropStage
    ): Result<Unit>
}
