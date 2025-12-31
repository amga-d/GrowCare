package com.example.growCare.data.local.datasource

import com.example.growCare.data.local.database.dao.CropDao
import com.example.growCare.data.local.database.entity.CropDataEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local data source for crop data
 * Handles all database operations for crops
 */
@Singleton
class CropLocalDataSource @Inject constructor(
    private val cropDao: CropDao
) {
    /**
     * Get all crops for a user
     */
    fun getUserCrops(userId: String): Flow<List<CropDataEntity>> {
        return cropDao.getUserCrops(userId)
    }
    
    /**
     * Get a specific crop by ID
     */
    suspend fun getCropById(cropId: String): CropDataEntity? {
        return cropDao.getCropById(cropId)
    }
    
    /**
     * Get active crops (not yet harvested)
     */
    fun getActiveCrops(userId: String): Flow<List<CropDataEntity>> {
        return cropDao.getActiveCrops(userId)
    }
    
    /**
     * Get crops by health status
     */
    fun getCropsByHealthStatus(userId: String, healthStatus: String): Flow<List<CropDataEntity>> {
        return cropDao.getCropsByHealthStatus(userId, healthStatus)
    }
    
    /**
     * Get crops by stage
     */
    fun getCropsByStage(userId: String, stage: String): Flow<List<CropDataEntity>> {
        return cropDao.getCropsByStage(userId, stage)
    }
    
    /**
     * Save a crop
     */
    suspend fun saveCrop(crop: CropDataEntity) {
        cropDao.insertCrop(crop)
    }
    
    /**
     * Save multiple crops
     */
    suspend fun saveCrops(crops: List<CropDataEntity>) {
        cropDao.insertCrops(crops)
    }
    
    /**
     * Update a crop
     */
    suspend fun updateCrop(crop: CropDataEntity) {
        cropDao.updateCrop(crop)
    }
    
    /**
     * Delete a crop
     */
    suspend fun deleteCrop(crop: CropDataEntity) {
        cropDao.deleteCrop(crop)
    }
    
    /**
     * Delete all crops for a user
     */
    suspend fun deleteUserCrops(userId: String) {
        cropDao.deleteUserCrops(userId)
    }
    
    /**
     * Search crops by name
     */
    fun searchCrops(userId: String, query: String): Flow<List<CropDataEntity>> {
        return cropDao.searchCrops(userId, query)
    }
    
    /**
     * Get total crop count
     */
    suspend fun getCropCount(userId: String): Int {
        return cropDao.getCropCount(userId)
    }
}
