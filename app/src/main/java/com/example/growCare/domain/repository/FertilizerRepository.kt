package com.example.growCare.domain.repository

import com.example.growCare.domain.model.FertilizerRecommendation
import com.example.growCare.domain.model.NPK
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for fertilizer calculation and recommendations
 * Handles NPK calculations and fertilizer suggestions
 */
interface FertilizerRepository {
    
    /**
     * Calculate fertilizer recommendation
     * @param cropType Type of crop
     * @param soilType Type of soil
     * @param area Farm area in acres
     * @param currentNPK Current NPK levels
     * @param targetYield Optional target yield
     * @return Result with FertilizerRecommendation
     */
    suspend fun calculateFertilizer(
        cropType: String,
        soilType: String,
        area: Double,
        currentNPK: NPK,
        targetYield: Double? = null
    ): Result<FertilizerRecommendation>
    
    /**
     * Get fertilizer calculation history
     * @return Flow of past fertilizer recommendations
     */
    fun getFertilizerHistory(): Flow<List<FertilizerRecommendation>>
    
    /**
     * Get a specific fertilizer recommendation by ID
     */
    suspend fun getFertilizerById(recommendationId: String): Result<FertilizerRecommendation?>
    
    /**
     * Save fertilizer recommendation to history
     */
    suspend fun saveFertilizerRecommendation(recommendation: FertilizerRecommendation): Result<Unit>
    
    /**
     * Delete a fertilizer recommendation
     */
    suspend fun deleteFertilizerRecommendation(recommendationId: String): Result<Unit>
    
    /**
     * Get NPK requirements for specific crop
     */
    suspend fun getCropNPKRequirements(cropType: String): Result<NPK>
    
    /**
     * Get organic fertilizer alternatives
     */
    suspend fun getOrganicAlternatives(npkRatio: NPK): Result<List<String>>
}
