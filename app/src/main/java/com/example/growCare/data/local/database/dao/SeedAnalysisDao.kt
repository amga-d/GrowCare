package com.example.growCare.data.local.database.dao

import androidx.room.*
import com.example.growCare.data.local.database.entity.SeedQualityEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for seed quality analyses
 * Provides methods for CRUD operations on seed quality analysis records
 */
@Dao
interface SeedAnalysisDao {
    
    /**
     * Get all seed analyses for a specific user
     * Returns a Flow for reactive updates
     */
    @Query("SELECT * FROM seed_analyses WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserAnalyses(userId: String): Flow<List<SeedQualityEntity>>
    
    /**
     * Get a specific analysis by ID
     */
    @Query("SELECT * FROM seed_analyses WHERE id = :analysisId")
    suspend fun getAnalysisById(analysisId: String): SeedQualityEntity?
    
    /**
     * Get analyses by seed type
     */
    @Query("SELECT * FROM seed_analyses WHERE userId = :userId AND seedType = :seedType ORDER BY timestamp DESC")
    fun getAnalysesBySeedType(userId: String, seedType: String): Flow<List<SeedQualityEntity>>
    
    /**
     * Get analyses recommended for use
     */
    @Query("SELECT * FROM seed_analyses WHERE userId = :userId AND isRecommendedForUse = 1 ORDER BY timestamp DESC")
    fun getRecommendedAnalyses(userId: String): Flow<List<SeedQualityEntity>>
    
    /**
     * Get analyses by quality score range
     */
    @Query("SELECT * FROM seed_analyses WHERE userId = :userId AND qualityScore >= :minScore AND qualityScore <= :maxScore ORDER BY timestamp DESC")
    fun getAnalysesByScoreRange(userId: String, minScore: Int, maxScore: Int): Flow<List<SeedQualityEntity>>
    
    /**
     * Get recent analyses (last N days)
     */
    @Query("SELECT * FROM seed_analyses WHERE userId = :userId AND timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getRecentAnalyses(userId: String, sinceTimestamp: Long): Flow<List<SeedQualityEntity>>
    
    /**
     * Insert a single analysis
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: SeedQualityEntity)
    
    /**
     * Insert multiple analyses
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyses(analyses: List<SeedQualityEntity>)
    
    /**
     * Update an analysis
     */
    @Update
    suspend fun updateAnalysis(analysis: SeedQualityEntity)
    
    /**
     * Delete an analysis
     */
    @Delete
    suspend fun deleteAnalysis(analysis: SeedQualityEntity)
    
    /**
     * Delete all analyses for a user
     */
    @Query("DELETE FROM seed_analyses WHERE userId = :userId")
    suspend fun deleteUserAnalyses(userId: String)
    
    /**
     * Get total count of analyses for a user
     */
    @Query("SELECT COUNT(*) FROM seed_analyses WHERE userId = :userId")
    suspend fun getAnalysisCount(userId: String): Int
    
    /**
     * Get average quality score for a user
     */
    @Query("SELECT AVG(qualityScore) FROM seed_analyses WHERE userId = :userId")
    suspend fun getAverageQualityScore(userId: String): Double?
}
