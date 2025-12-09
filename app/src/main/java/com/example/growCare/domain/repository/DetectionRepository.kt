package com.example.growCare.domain.repository

import android.net.Uri
import com.example.growCare.domain.model.DiseaseAnalysis
import com.example.growCare.domain.model.SeedQuality
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for disease detection and seed quality analysis
 * Handles AI-powered image analysis for plant health
 */
interface DetectionRepository {
    
    /**
     * Analyze plant image for disease detection
     * @param imageUri URI of the plant image
     * @param cropName Optional crop name for context
     * @return Result with DiseaseAnalysis
     */
    suspend fun analyzePlantDisease(
        imageUri: Uri,
        cropName: String? = null
    ): Result<DiseaseAnalysis>
    
    /**
     * Analyze seed quality from image
     * @param imageUri URI of the seed image
     * @param seedType Type of seed being analyzed
     * @return Result with SeedQuality analysis
     */
    suspend fun analyzeSeedQuality(
        imageUri: Uri,
        seedType: String
    ): Result<SeedQuality>
    
    /**
     * Get disease scan history
     * @return Flow of disease analyses, sorted by timestamp
     */
    fun getDiseaseHistory(): Flow<List<DiseaseAnalysis>>
    
    /**
     * Get seed scan history
     * @return Flow of seed quality analyses
     */
    fun getSeedHistory(): Flow<List<SeedQuality>>
    
    /**
     * Get a specific disease analysis by ID
     */
    suspend fun getDiseaseAnalysisById(analysisId: String): Result<DiseaseAnalysis?>
    
    /**
     * Get a specific seed analysis by ID
     */
    suspend fun getSeedAnalysisById(analysisId: String): Result<SeedQuality?>
    
    /**
     * Delete a disease analysis record
     */
    suspend fun deleteDiseaseAnalysis(analysisId: String): Result<Unit>
    
    /**
     * Delete a seed analysis record
     */
    suspend fun deleteSeedAnalysis(analysisId: String): Result<Unit>
    
    /**
     * Save disease analysis to history
     */
    suspend fun saveDiseaseAnalysis(analysis: DiseaseAnalysis): Result<Unit>
    
    /**
     * Save seed analysis to history
     */
    suspend fun saveSeedAnalysis(analysis: SeedQuality): Result<Unit>
    
    /**
     * Get disease analyses by severity
     */
    fun getDiseasesBySeverity(
        severity: com.example.growCare.domain.model.DiseaseSeverity
    ): Flow<List<DiseaseAnalysis>>
    
    /**
     * Get seed analyses by quality score range
     */
    fun getSeedsByQualityRange(minScore: Int, maxScore: Int): Flow<List<SeedQuality>>
}
