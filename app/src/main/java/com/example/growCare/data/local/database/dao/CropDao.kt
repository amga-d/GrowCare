package com.example.growCare.data.local.database.dao

import androidx.room.*
import com.example.growCare.data.local.database.entity.CropDataEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for crop data
 * Provides methods for CRUD operations on crop records
 */
@Dao
interface CropDao {
    
    /**
     * Get all crops for a specific user
     * Returns a Flow for reactive updates
     */
    @Query("SELECT * FROM crops WHERE userId = :userId ORDER BY plantedDate DESC")
    fun getUserCrops(userId: String): Flow<List<CropDataEntity>>
    
    /**
     * Get a specific crop by ID
     */
    @Query("SELECT * FROM crops WHERE id = :cropId")
    suspend fun getCropById(cropId: String): CropDataEntity?
    
    /**
     * Get crops by health status
     */
    @Query("SELECT * FROM crops WHERE userId = :userId AND healthStatus = :healthStatus ORDER BY plantedDate DESC")
    fun getCropsByHealthStatus(userId: String, healthStatus: String): Flow<List<CropDataEntity>>
    
    /**
     * Get crops by current stage
     */
    @Query("SELECT * FROM crops WHERE userId = :userId AND currentStage = :stage ORDER BY plantedDate DESC")
    fun getCropsByStage(userId: String, stage: String): Flow<List<CropDataEntity>>
    
    /**
     * Get active crops (not yet harvested)
     */
    @Query("SELECT * FROM crops WHERE userId = :userId AND actualHarvestDate IS NULL ORDER BY plantedDate DESC")
    fun getActiveCrops(userId: String): Flow<List<CropDataEntity>>
    
    /**
     * Insert a single crop
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropDataEntity)
    
    /**
     * Insert multiple crops
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropDataEntity>)
    
    /**
     * Update a crop
     */
    @Update
    suspend fun updateCrop(crop: CropDataEntity)
    
    /**
     * Delete a crop
     */
    @Delete
    suspend fun deleteCrop(crop: CropDataEntity)
    
    /**
     * Delete all crops for a user
     */
    @Query("DELETE FROM crops WHERE userId = :userId")
    suspend fun deleteUserCrops(userId: String)
    
    /**
     * Get total count of crops for a user
     */
    @Query("SELECT COUNT(*) FROM crops WHERE userId = :userId")
    suspend fun getCropCount(userId: String): Int
    
    /**
     * Search crops by name
     */
    @Query("SELECT * FROM crops WHERE userId = :userId AND cropName LIKE '%' || :searchQuery || '%' ORDER BY plantedDate DESC")
    fun searchCrops(userId: String, searchQuery: String): Flow<List<CropDataEntity>>
}
