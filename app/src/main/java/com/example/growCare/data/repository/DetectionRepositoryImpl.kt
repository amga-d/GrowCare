package com.example.growCare.data.repository

import android.net.Uri
import com.example.growCare.data.remote.firebase.FirebaseStorageDataSource
import com.example.growCare.data.remote.firebase.FirestoreDataSource
import com.example.growCare.data.remote.gemini.GeminiAIService
import com.example.growCare.domain.model.DiseaseAnalysis
import com.example.growCare.domain.model.DiseaseSeverity
import com.example.growCare.domain.model.SeedQuality
import com.example.growCare.domain.model.ColorConsistency
import com.example.growCare.domain.model.SeedSize
import com.example.growCare.domain.model.DamageType
import com.example.growCare.domain.repository.DetectionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DetectionRepository
 * Handles disease detection and seed quality analysis using Gemini AI
 */
@Singleton
class DetectionRepositoryImpl @Inject constructor(
    private val geminiAIService: GeminiAIService,
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    private val auth: FirebaseAuth
) : DetectionRepository {

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    override suspend fun analyzePlantDisease(
        imageUri: Uri,
        cropName: String?
    ): Result<DiseaseAnalysis> {
        return try {
            val userId = getCurrentUserId()
            
            // Step 1: Upload image to Firebase Storage
            val imageUploadResult = storageDataSource.uploadDiseaseImage(userId, imageUri)
            if (imageUploadResult.isFailure) {
                return Result.failure(imageUploadResult.exceptionOrNull() 
                    ?: Exception("Failed to upload image"))
            }
            val imageUrl = imageUploadResult.getOrThrow()
            
            // Step 2: Analyze with Gemini AI
            val analysisResult = geminiAIService.analyzePlantDisease(imageUri)
            if (analysisResult.isFailure) {
                return Result.failure(analysisResult.exceptionOrNull() 
                    ?: Exception("Failed to analyze image"))
            }
            val analysisJson = analysisResult.getOrThrow()
            
            // Step 3: Parse AI response into DiseaseAnalysis object
            val diseaseDto = geminiAIService.parseDiseaseAnalysisJson(analysisJson)
            val diseaseAnalysis = DiseaseAnalysis(
                id = UUID.randomUUID().toString(),
                userId = userId,
                cropName = cropName,
                imageUrl = imageUrl,
                diseaseName = diseaseDto.diseaseName,
                confidence = diseaseDto.confidence,
                symptoms = diseaseDto.symptoms,
                severity = DiseaseSeverity.valueOf(diseaseDto.severity),
                treatment = diseaseDto.treatment,
                prevention = diseaseDto.prevention,
                additionalNotes = diseaseDto.additionalNotes,
                timestamp = System.currentTimeMillis()
            )
            
            // Step 4: Save to Firestore
            val saveResult = firestoreDataSource.saveDiseaseAnalysis(
                userId = userId,
                scanId = diseaseAnalysis.id,
                analysisData = diseaseAnalysisToMap(diseaseAnalysis)
            )
            
            if (saveResult.isFailure) {
                // Still return the analysis even if save fails
                // Just log the error or handle it appropriately
            }
            
            Result.success(diseaseAnalysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun analyzeSeedQuality(
        imageUri: Uri,
        seedType: String
    ): Result<SeedQuality> {
        return try {
            val userId = getCurrentUserId()
            
            // Step 1: Upload image to Firebase Storage
            val imageUploadResult = storageDataSource.uploadSeedImage(userId, imageUri)
            if (imageUploadResult.isFailure) {
                return Result.failure(imageUploadResult.exceptionOrNull() 
                    ?: Exception("Failed to upload image"))
            }
            val imageUrl = imageUploadResult.getOrThrow()
            
            // Step 2: Analyze with Gemini AI
            val analysisResult = geminiAIService.analyzeSeedQuality(imageUri, seedType)
            if (analysisResult.isFailure) {
                return Result.failure(analysisResult.exceptionOrNull() 
                    ?: Exception("Failed to analyze image"))
            }
            val analysisJson = analysisResult.getOrThrow()
            
            // Step 3: Parse AI response into SeedQuality object
            val seedDto = geminiAIService.parseSeedQualityJson(analysisJson)
            val seedQuality = SeedQuality(
                id = UUID.randomUUID().toString(),
                userId = userId,
                seedType = seedType,
                imageUrl = imageUrl,
                qualityScore = seedDto.qualityScore,
                sizeAssessment = SeedSize.valueOf(seedDto.sizeAssessment),
                colorConsistency = ColorConsistency.valueOf(seedDto.colorConsistency),
                damagePercentage = seedDto.damagePercentage,
                damageTypes = seedDto.damageTypes.map { DamageType.valueOf(it) },
                germinationPotential = seedDto.germinationPotential,
                recommendations = seedDto.recommendations,
                storageAdvice = seedDto.storageAdvice,
                isRecommendedForUse = seedDto.isRecommendedForUse,
                timestamp = System.currentTimeMillis()
            )
            
            // Step 4: Save to Firestore
            val saveResult = firestoreDataSource.saveSeedAnalysis(
                userId = userId,
                scanId = seedQuality.id,
                analysisData = seedQualityToMap(seedQuality)
            )
            
            if (saveResult.isFailure) {
                // Still return the analysis even if save fails
            }
            
            Result.success(seedQuality)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDiseaseHistory(): Flow<List<DiseaseAnalysis>> = flow {
        try {
            val userId = getCurrentUserId()
            firestoreDataSource.getDiseaseScansStream(userId).collect { dataList ->
                val analyses = dataList.mapNotNull { data ->
                    try {
                        mapToDiseaseAnalysis(data)
                    } catch (e: Exception) {
                        null // Skip invalid entries
                    }
                }
                emit(analyses)
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getSeedHistory(): Flow<List<SeedQuality>> = flow {
        try {
            val userId = getCurrentUserId()
            firestoreDataSource.getSeedScansStream(userId).collect { dataList ->
                val analyses = dataList.mapNotNull { data ->
                    try {
                        mapToSeedQuality(data)
                    } catch (e: Exception) {
                        null // Skip invalid entries
                    }
                }
                emit(analyses)
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getDiseaseAnalysisById(id: String): Result<DiseaseAnalysis?> {
        return try {
            val userId = getCurrentUserId()
            // Get from Firestore
            val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("disease_scans")
                .document(id)
                .get()
                .await()
            
            val data = doc.data
            Result.success(data?.let { mapToDiseaseAnalysis(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSeedAnalysisById(id: String): Result<SeedQuality?> {
        return try {
            val userId = getCurrentUserId()
            // Get from Firestore
            val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("seed_scans")
                .document(id)
                .get()
                .await()
            
            val data = doc.data
            Result.success(data?.let { mapToSeedQuality(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDiseaseAnalysis(id: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("disease_scans")
                .document(id)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSeedAnalysis(id: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("seed_scans")
                .document(id)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveDiseaseAnalysis(analysis: DiseaseAnalysis): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            firestoreDataSource.saveDiseaseAnalysis(
                userId = userId,
                scanId = analysis.id,
                analysisData = diseaseAnalysisToMap(analysis)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSeedAnalysis(analysis: SeedQuality): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            firestoreDataSource.saveSeedAnalysis(
                userId = userId,
                scanId = analysis.id,
                analysisData = seedQualityToMap(analysis)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDiseasesBySeverity(severity: DiseaseSeverity): Flow<List<DiseaseAnalysis>> = flow {
        getDiseaseHistory().collect { analyses ->
            emit(analyses.filter { it.severity == severity })
        }
    }

    override fun getSeedsByQualityRange(minScore: Int, maxScore: Int): Flow<List<SeedQuality>> = flow {
        getSeedHistory().collect { seeds ->
            emit(seeds.filter { it.qualityScore in minScore..maxScore })
        }
    }

    // ============ Private Helper Functions ============

    /**
     * Parse Gemini's text response into DiseaseAnalysis object
     * Handles both structured and unstructured responses
     */
    private fun parseDiseaseAnalysis(
        analysisText: String,
        userId: String,
        imageUrl: String,
        cropName: String?
    ): DiseaseAnalysis {
        val lines = analysisText.lines().filter { it.isNotBlank() }
        
        var diseaseName = "Unknown Disease"
        var confidence = 50
        val symptoms = mutableListOf<String>()
        var severity = DiseaseSeverity.MODERATE
        val treatment = mutableListOf<String>()
        val prevention = mutableListOf<String>()
        var additionalNotes: String? = null
        
        var currentSection = ""
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // Detect section headers
            when {
                trimmedLine.contains("Disease Identification", ignoreCase = true) ||
                trimmedLine.contains("Disease Name", ignoreCase = true) -> {
                    currentSection = "disease"
                    // Extract disease name if on same line
                    val colonIndex = trimmedLine.indexOf(':')
                    if (colonIndex >= 0 && colonIndex < trimmedLine.length - 1) {
                        diseaseName = trimmedLine.substring(colonIndex + 1).trim()
                            .removePrefix("**").removeSuffix("**")
                    }
                }
                trimmedLine.contains("Confidence", ignoreCase = true) -> {
                    currentSection = "confidence"
                    // Extract confidence number
                    val numbers = Regex("\\d+").findAll(trimmedLine).toList()
                    if (numbers.isNotEmpty()) {
                        confidence = numbers.first().value.toIntOrNull()?.coerceIn(0, 100) ?: 50
                    }
                }
                trimmedLine.contains("Symptoms", ignoreCase = true) -> {
                    currentSection = "symptoms"
                }
                trimmedLine.contains("Severity", ignoreCase = true) -> {
                    currentSection = "severity"
                    // Extract severity
                    when {
                        trimmedLine.contains("mild", ignoreCase = true) -> severity = DiseaseSeverity.MILD
                        trimmedLine.contains("moderate", ignoreCase = true) -> severity = DiseaseSeverity.MODERATE
                        trimmedLine.contains("severe", ignoreCase = true) -> severity = DiseaseSeverity.SEVERE
                    }
                }
                trimmedLine.contains("Treatment", ignoreCase = true) -> {
                    currentSection = "treatment"
                }
                trimmedLine.contains("Prevention", ignoreCase = true) -> {
                    currentSection = "prevention"
                }
                trimmedLine.contains("Additional Notes", ignoreCase = true) ||
                trimmedLine.contains("Notes", ignoreCase = true) -> {
                    currentSection = "notes"
                }
                else -> {
                    // Add content to appropriate section
                    val content = trimmedLine.removePrefix("-").removePrefix("*").trim()
                    if (content.isNotEmpty() && !content.startsWith("#") && !content.startsWith("**")) {
                        when (currentSection) {
                            "disease" -> if (diseaseName == "Unknown Disease") diseaseName = content
                            "symptoms" -> symptoms.add(content)
                            "treatment" -> treatment.add(content)
                            "prevention" -> prevention.add(content)
                            "notes" -> additionalNotes = (additionalNotes ?: "") + content + " "
                        }
                    }
                }
            }
        }
        
        // Fallback: If no disease name found, check for "Healthy" or "No disease"
        if (diseaseName == "Unknown Disease") {
            if (analysisText.contains("healthy", ignoreCase = true) ||
                analysisText.contains("no disease", ignoreCase = true)) {
                diseaseName = "Healthy"
                severity = DiseaseSeverity.MILD
            }
        }
        
        return DiseaseAnalysis(
            id = UUID.randomUUID().toString(),
            userId = userId,
            cropName = cropName,
            imageUrl = imageUrl,
            diseaseName = diseaseName,
            confidence = confidence,
            symptoms = symptoms.ifEmpty { listOf("No specific symptoms identified") },
            severity = severity,
            treatment = treatment.ifEmpty { listOf("Consult agricultural expert for treatment") },
            prevention = prevention.ifEmpty { listOf("Maintain good crop health practices") },
            additionalNotes = additionalNotes?.trim()
        )
    }

    /**
     * Parse Gemini's text response into SeedQuality object
     */
    private fun parseSeedQuality(
        analysisText: String,
        userId: String,
        imageUrl: String,
        seedType: String
    ): SeedQuality {
        val lines = analysisText.lines().filter { it.isNotBlank() }
        
        var qualityScore = 50
        var size = SeedSize.MEDIUM
        var colorConsistency = ColorConsistency.SLIGHTLY_VARIED
        var damagePercentage = 10
        val damageTypes = mutableListOf<DamageType>()
        var germinationPotential = 70
        val recommendations = mutableListOf<String>()
        
        var currentSection = ""
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine.contains("Quality Score", ignoreCase = true) -> {
                    currentSection = "quality"
                    val numbers = Regex("\\d+").findAll(trimmedLine).toList()
                    if (numbers.isNotEmpty()) {
                        qualityScore = numbers.first().value.toIntOrNull()?.coerceIn(0, 100) ?: 50
                    }
                }
                trimmedLine.contains("Size Assessment", ignoreCase = true) -> {
                    currentSection = "size"
                    size = when {
                        trimmedLine.contains("small", ignoreCase = true) -> SeedSize.SMALL
                        trimmedLine.contains("large", ignoreCase = true) -> SeedSize.LARGE
                        trimmedLine.contains("mixed", ignoreCase = true) -> SeedSize.MIXED
                        else -> SeedSize.MEDIUM
                    }
                }
                trimmedLine.contains("Color Consistency", ignoreCase = true) -> {
                    currentSection = "color"
                    colorConsistency = when {
                        trimmedLine.contains("uniform", ignoreCase = true) -> ColorConsistency.UNIFORM
                        trimmedLine.contains("highly varied", ignoreCase = true) -> ColorConsistency.HIGHLY_VARIED
                        else -> ColorConsistency.SLIGHTLY_VARIED
                    }
                }
                trimmedLine.contains("Damage", ignoreCase = true) -> {
                    currentSection = "damage"
                    val numbers = Regex("\\d+").findAll(trimmedLine).toList()
                    if (numbers.isNotEmpty()) {
                        damagePercentage = numbers.first().value.toIntOrNull()?.coerceIn(0, 100) ?: 10
                    }
                    // Detect damage types
                    if (trimmedLine.contains("insect", ignoreCase = true)) damageTypes.add(DamageType.INSECT)
                    if (trimmedLine.contains("fungal", ignoreCase = true)) damageTypes.add(DamageType.FUNGAL)
                    if (trimmedLine.contains("mechanical", ignoreCase = true)) damageTypes.add(DamageType.MECHANICAL)
                }
                trimmedLine.contains("Germination", ignoreCase = true) -> {
                    currentSection = "germination"
                    val numbers = Regex("\\d+").findAll(trimmedLine).toList()
                    if (numbers.isNotEmpty()) {
                        germinationPotential = numbers.first().value.toIntOrNull()?.coerceIn(0, 100) ?: 70
                    }
                }
                trimmedLine.contains("Recommendation", ignoreCase = true) -> {
                    currentSection = "recommendations"
                }
                else -> {
                    val content = trimmedLine.removePrefix("-").removePrefix("*").trim()
                    if (content.isNotEmpty() && currentSection == "recommendations" && 
                        !content.startsWith("#") && !content.startsWith("**")) {
                        recommendations.add(content)
                    }
                }
            }
        }
        
        if (damageTypes.isEmpty() && damagePercentage == 0) {
            damageTypes.add(DamageType.NONE)
        }
        
        return SeedQuality(
            id = UUID.randomUUID().toString(),
            userId = userId,
            seedType = seedType,
            imageUrl = imageUrl,
            qualityScore = qualityScore,
            sizeAssessment = size,
            colorConsistency = colorConsistency,
            damagePercentage = damagePercentage,
            damageTypes = damageTypes.ifEmpty { listOf(DamageType.NONE) },
            germinationPotential = germinationPotential,
            recommendations = recommendations.ifEmpty { listOf("Seeds appear suitable for planting") },
            isRecommendedForUse = qualityScore >= 60 && germinationPotential >= 60
        )
    }

    /**
     * Convert DiseaseAnalysis to Map for Firestore
     */
    private fun diseaseAnalysisToMap(analysis: DiseaseAnalysis): Map<String, Any> {
        return mapOf(
            "id" to analysis.id,
            "userId" to analysis.userId,
            "cropName" to (analysis.cropName ?: ""),
            "imageUrl" to analysis.imageUrl,
            "diseaseName" to analysis.diseaseName,
            "confidence" to analysis.confidence,
            "symptoms" to analysis.symptoms,
            "severity" to analysis.severity.name,
            "treatment" to analysis.treatment,
            "prevention" to analysis.prevention,
            "additionalNotes" to (analysis.additionalNotes ?: ""),
            "timestamp" to analysis.timestamp
        )
    }

    /**
     * Convert SeedQuality to Map for Firestore
     */
    private fun seedQualityToMap(seedQuality: SeedQuality): Map<String, Any> {
        return mapOf(
            "id" to seedQuality.id,
            "userId" to seedQuality.userId,
            "seedType" to seedQuality.seedType,
            "imageUrl" to seedQuality.imageUrl,
            "qualityScore" to seedQuality.qualityScore,
            "sizeAssessment" to seedQuality.sizeAssessment.name,
            "colorConsistency" to seedQuality.colorConsistency.name,
            "damagePercentage" to seedQuality.damagePercentage,
            "damageTypes" to seedQuality.damageTypes.map { it.name },
            "germinationPotential" to seedQuality.germinationPotential,
            "recommendations" to seedQuality.recommendations,
            "timestamp" to seedQuality.timestamp
        )
    }

    /**
     * Convert Firestore Map to DiseaseAnalysis
     */
    private fun mapToDiseaseAnalysis(data: Map<String, Any>): DiseaseAnalysis {
        return DiseaseAnalysis(
            id = data["id"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            cropName = data["cropName"] as? String,
            imageUrl = data["imageUrl"] as? String ?: "",
            diseaseName = data["diseaseName"] as? String ?: "Unknown",
            confidence = (data["confidence"] as? Long)?.toInt() ?: 50,
            symptoms = (data["symptoms"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            severity = DiseaseSeverity.valueOf(data["severity"] as? String ?: "MODERATE"),
            treatment = (data["treatment"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            prevention = (data["prevention"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            additionalNotes = data["additionalNotes"] as? String,
            timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
        )
    }

    /**
     * Convert Firestore Map to SeedQuality
     */
    private fun mapToSeedQuality(data: Map<String, Any>): SeedQuality {
        return SeedQuality(
            id = data["id"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            seedType = data["seedType"] as? String ?: "Unknown",
            imageUrl = data["imageUrl"] as? String ?: "",
            qualityScore = (data["qualityScore"] as? Long)?.toInt() ?: 50,
            sizeAssessment = SeedSize.valueOf(data["sizeAssessment"] as? String ?: "MEDIUM"),
            colorConsistency = ColorConsistency.valueOf(data["colorConsistency"] as? String ?: "SLIGHTLY_VARIED"),
            damagePercentage = (data["damagePercentage"] as? Long)?.toInt() ?: 0,
            damageTypes = (data["damageTypes"] as? List<*>)?.mapNotNull { 
                try { DamageType.valueOf(it as String) } catch (e: Exception) { null }
            } ?: listOf(DamageType.NONE),
            germinationPotential = (data["germinationPotential"] as? Long)?.toInt() ?: 70,
            recommendations = (data["recommendations"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            isRecommendedForUse = (data["isRecommendedForUse"] as? Boolean) ?: true,
            timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
        )
    }
}
