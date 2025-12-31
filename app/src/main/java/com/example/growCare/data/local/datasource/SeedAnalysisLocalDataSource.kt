package com.example.growCare.data.local.datasource

import com.example.growCare.data.local.database.dao.SeedAnalysisDao
import com.example.growCare.data.local.database.entity.SeedQualityEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local data source for seed quality analyses
 * Handles all database operations for seed quality analysis history
 */
@Singleton
class SeedAnalysisLocalDataSource @Inject constructor(
    private val seedAnalysisDao: SeedAnalysisDao
) {
    /**
     * Get all analyses for a user
     */
    fun getUserAnalyses(userId: String): Flow<List<SeedQualityEntity>> {
        return seedAnalysisDao.getUserAnalyses(userId)
    }
    
    /**
     * Get a specific analysis by ID
     */
    suspend fun getAnalysisById(analysisId: String): SeedQualityEntity? {
        return seedAnalysisDao.getAnalysisById(analysisId)
    }
    
    /**
     * Get analyses by seed type
     */
    fun getAnalysesBySeedType(userId: String, seedType: String): Flow<List<SeedQualityEntity>> {
        return seedAnalysisDao.getAnalysesBySeedType(userId, seedType)
    }
    
    /**
     * Get recommended analyses
     */
    fun getRecommendedAnalyses(userId: String): Flow<List<SeedQualityEntity>> {
        return seedAnalysisDao.getRecommendedAnalyses(userId)
    }
    
    /**
     * Get analyses by score range
     */
    fun getAnalysesByScoreRange(userId: String, minScore: Int, maxScore: Int): Flow<List<SeedQualityEntity>> {
        return seedAnalysisDao.getAnalysesByScoreRange(userId, minScore, maxScore)
    }
    
    /**
     * Get recent analyses
     */
    fun getRecentAnalyses(userId: String, sinceTimestamp: Long): Flow<List<SeedQualityEntity>> {
        return seedAnalysisDao.getRecentAnalyses(userId, sinceTimestamp)
    }
    
    /**
     * Save an analysis
     */
    suspend fun saveAnalysis(analysis: SeedQualityEntity) {
        seedAnalysisDao.insertAnalysis(analysis)
    }
    
    /**
     * Save multiple analyses
     */
    suspend fun saveAnalyses(analyses: List<SeedQualityEntity>) {
        seedAnalysisDao.insertAnalyses(analyses)
    }
    
    /**
     * Update an analysis
     */
    suspend fun updateAnalysis(analysis: SeedQualityEntity) {
        seedAnalysisDao.updateAnalysis(analysis)
    }
    
    /**
     * Delete an analysis
     */
    suspend fun deleteAnalysis(analysis: SeedQualityEntity) {
        seedAnalysisDao.deleteAnalysis(analysis)
    }
    
    /**
     * Delete all analyses for a user
     */
    suspend fun deleteUserAnalyses(userId: String) {
        seedAnalysisDao.deleteUserAnalyses(userId)
    }
    
    /**
     * Get total analysis count
     */
    suspend fun getAnalysisCount(userId: String): Int {
        return seedAnalysisDao.getAnalysisCount(userId)
    }
    
    /**
     * Get average quality score
     */
    suspend fun getAverageQualityScore(userId: String): Double? {
        return seedAnalysisDao.getAverageQualityScore(userId)
    }
}
