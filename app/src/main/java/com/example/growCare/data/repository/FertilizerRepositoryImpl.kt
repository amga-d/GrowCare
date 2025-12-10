package com.example.growCare.data.repository

import com.example.growCare.data.remote.firebase.FirestoreDataSource
import com.example.growCare.data.remote.gemini.GeminiClient
import com.example.growCare.domain.model.*
import com.example.growCare.domain.repository.FertilizerRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FertilizerRepository
 * Handles fertilizer calculations using Gemini AI
 */
@Singleton
class FertilizerRepositoryImpl @Inject constructor(
    private val geminiClient: GeminiClient,
    private val firestoreDataSource: FirestoreDataSource,
    private val auth: FirebaseAuth
) : FertilizerRepository {

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    override suspend fun calculateFertilizer(
        cropType: String,
        soilType: String,
        area: Double,
        currentNPK: NPK,
        targetYield: Double?
    ): Result<FertilizerRecommendation> {
        return try {
            val userId = getCurrentUserId()
            
            // Step 1: Get recommendation from Gemini AI
            val recommendationResult = geminiClient.getFertilizerRecommendation(
                cropType = cropType,
                soilType = soilType,
                area = area,
                currentNPK = currentNPK.toRatioString()
            )
            
            if (recommendationResult.isFailure) {
                return Result.failure(recommendationResult.exceptionOrNull() 
                    ?: Exception("Failed to calculate fertilizer"))
            }
            
            val analysisText = recommendationResult.getOrThrow()
            
            // Step 2: Parse AI response into FertilizerRecommendation
            val recommendation = parseFertilizerRecommendation(
                analysisText = analysisText,
                userId = userId,
                cropType = cropType,
                soilType = soilType,
                area = area,
                currentNPK = currentNPK,
                targetYield = targetYield
            )
            
            // Step 3: Save to Firestore
            val saveResult = saveFertilizerRecommendation(recommendation)
            if (saveResult.isFailure) {
                // Still return the recommendation even if save fails
            }
            
            Result.success(recommendation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFertilizerHistory(): Flow<List<FertilizerRecommendation>> = flow {
        try {
            val userId = getCurrentUserId()
            
            firestoreDataSource.getFertilizerCalculationsStream(userId).collect { dataList ->
                val recommendations = dataList.mapNotNull { data ->
                    try {
                        mapToFertilizerRecommendation(data)
                    } catch (e: Exception) {
                        null
                    }
                }
                emit(recommendations)
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getFertilizerById(recommendationId: String): Result<FertilizerRecommendation?> {
        return try {
            val userId = getCurrentUserId()
            // Get from Firestore directly
            val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("fertilizer_calculations")
                .document(recommendationId)
                .get()
                .await()
            
            val data = doc.data
            Result.success(data?.let { mapToFertilizerRecommendation(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFertilizerRecommendation(
        recommendation: FertilizerRecommendation
    ): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            firestoreDataSource.saveFertilizerCalculation(
                userId = userId,
                calculationId = recommendation.id,
                calculationData = fertilizerRecommendationToMap(recommendation)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFertilizerRecommendation(recommendationId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("fertilizer_calculations")
                .document(recommendationId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCropNPKRequirements(cropType: String): Result<NPK> {
        // Predefined NPK requirements for common crops
        val requirements = when (cropType.lowercase()) {
            "corn", "maize" -> NPK(nitrogen = 150.0, phosphorus = 60.0, potassium = 40.0)
            "wheat" -> NPK(nitrogen = 120.0, phosphorus = 60.0, potassium = 40.0)
            "rice" -> NPK(nitrogen = 100.0, phosphorus = 50.0, potassium = 50.0)
            "soybean" -> NPK(nitrogen = 30.0, phosphorus = 60.0, potassium = 60.0)
            "cotton" -> NPK(nitrogen = 100.0, phosphorus = 50.0, potassium = 60.0)
            "tomato" -> NPK(nitrogen = 150.0, phosphorus = 80.0, potassium = 180.0)
            "potato" -> NPK(nitrogen = 120.0, phosphorus = 80.0, potassium = 150.0)
            "sugarcane" -> NPK(nitrogen = 200.0, phosphorus = 80.0, potassium = 100.0)
            else -> NPK(nitrogen = 100.0, phosphorus = 50.0, potassium = 50.0) // Default
        }
        return Result.success(requirements)
    }

    override suspend fun getOrganicAlternatives(npkRatio: NPK): Result<List<String>> {
        val alternatives = mutableListOf<String>()
        
        // Nitrogen sources
        if (npkRatio.nitrogen > 0) {
            alternatives.add("Compost (2-1-1)")
            alternatives.add("Blood meal (12-0-0)")
            alternatives.add("Fish emulsion (5-1-1)")
        }
        
        // Phosphorus sources
        if (npkRatio.phosphorus > 0) {
            alternatives.add("Bone meal (3-15-0)")
            alternatives.add("Rock phosphate (0-3-0)")
        }
        
        // Potassium sources
        if (npkRatio.potassium > 0) {
            alternatives.add("Wood ash (0-1-10)")
            alternatives.add("Kelp meal (1-0-2)")
            alternatives.add("Greensand (0-0-3)")
        }
        
        // General organic fertilizers
        alternatives.add("Vermicompost (1-1-1)")
        alternatives.add("Manure (varies by type)")
        
        return Result.success(alternatives)
    }

    // ============ Private Helper Functions ============

    /**
     * Parse Gemini's text response into FertilizerRecommendation object
     */
    private fun parseFertilizerRecommendation(
        analysisText: String,
        userId: String,
        cropType: String,
        soilType: String,
        area: Double,
        currentNPK: NPK,
        targetYield: Double?
    ): FertilizerRecommendation {
        val lines = analysisText.lines().filter { it.isNotBlank() }
        
        var recommendedN = 100.0
        var recommendedP = 50.0
        var recommendedK = 50.0
        val products = mutableListOf<FertilizerProduct>()
        val schedule = mutableListOf<ApplicationPhase>()
        var estimatedCost = 0.0
        val organicAlts = mutableListOf<String>()
        var additionalNotes = ""
        
        var currentSection = ""
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                trimmedLine.contains("Recommended NPK", ignoreCase = true) ||
                trimmedLine.contains("NPK Requirements", ignoreCase = true) -> {
                    currentSection = "npk"
                    // Try to extract NPK values
                    val numbers = Regex("\\d+\\.?\\d*").findAll(trimmedLine).toList()
                    if (numbers.size >= 3) {
                        recommendedN = numbers[0].value.toDoubleOrNull() ?: recommendedN
                        recommendedP = numbers[1].value.toDoubleOrNull() ?: recommendedP
                        recommendedK = numbers[2].value.toDoubleOrNull() ?: recommendedK
                    }
                }
                trimmedLine.contains("Product", ignoreCase = true) && 
                !trimmedLine.contains("organic", ignoreCase = true) -> {
                    currentSection = "products"
                }
                trimmedLine.contains("Schedule", ignoreCase = true) ||
                trimmedLine.contains("Application", ignoreCase = true) -> {
                    currentSection = "schedule"
                }
                trimmedLine.contains("Cost", ignoreCase = true) -> {
                    currentSection = "cost"
                    // Extract cost
                    val numbers = Regex("\\d+\\.?\\d*").findAll(trimmedLine).toList()
                    if (numbers.isNotEmpty()) {
                        estimatedCost = numbers.first().value.toDoubleOrNull() ?: 0.0
                    }
                }
                trimmedLine.contains("Organic", ignoreCase = true) -> {
                    currentSection = "organic"
                }
                trimmedLine.contains("Notes", ignoreCase = true) ||
                trimmedLine.contains("Additional", ignoreCase = true) -> {
                    currentSection = "notes"
                }
                else -> {
                    val content = trimmedLine.removePrefix("-").removePrefix("*").trim()
                    if (content.isNotEmpty() && !content.startsWith("#") && !content.startsWith("**")) {
                        when (currentSection) {
                            "products" -> {
                                // Try to parse product info
                                if (content.isNotBlank()) {
                                    // Simple product extraction
                                    val numbers = Regex("\\d+").findAll(content).toList()
                                    if (numbers.size >= 3) {
                                        val n = numbers[0].value.toDoubleOrNull() ?: 0.0
                                        val p = numbers[1].value.toDoubleOrNull() ?: 0.0
                                        val k = numbers[2].value.toDoubleOrNull() ?: 0.0
                                        
                                        products.add(FertilizerProduct(
                                            name = content.split("-").firstOrNull()?.trim() ?: "NPK Fertilizer",
                                            npkRatio = NPK(n, p, k),
                                            quantityNeeded = area * 50, // Rough estimate
                                            pricePerKg = 2.5,
                                            totalCost = area * 50 * 2.5,
                                            applicationMethod = "Broadcast and incorporate",
                                            isOrganic = false
                                        ))
                                    }
                                }
                            }
                            "schedule" -> {
                                // Add to schedule if contains days or phase info
                                if (content.contains("day", ignoreCase = true) || 
                                    content.contains("planting", ignoreCase = true)) {
                                    val numbers = Regex("\\d+").findAll(content).toList()
                                    val days = numbers.firstOrNull()?.value?.toIntOrNull() ?: 0
                                    
                                    schedule.add(ApplicationPhase(
                                        phase = content.split("-").firstOrNull()?.trim() ?: "Application",
                                        daysAfterPlanting = days,
                                        npkRatio = NPK(recommendedN / 3, recommendedP / 3, recommendedK / 3),
                                        quantity = area * 20,
                                        instructions = content
                                    ))
                                }
                            }
                            "organic" -> {
                                if (content.isNotBlank()) {
                                    organicAlts.add(content)
                                }
                            }
                            "notes" -> {
                                additionalNotes += "$content "
                            }
                        }
                    }
                }
            }
        }
        
        // If no products found, create a default one
        if (products.isEmpty()) {
            products.add(FertilizerProduct(
                name = "NPK ${recommendedN.toInt()}-${recommendedP.toInt()}-${recommendedK.toInt()}",
                npkRatio = NPK(recommendedN, recommendedP, recommendedK),
                quantityNeeded = area * 50,
                pricePerKg = 2.5,
                totalCost = area * 50 * 2.5,
                applicationMethod = "Broadcast evenly and incorporate into soil",
                isOrganic = false
            ))
            estimatedCost = area * 50 * 2.5
        }
        
        // If no schedule, create basic one
        if (schedule.isEmpty()) {
            schedule.add(ApplicationPhase(
                phase = "Pre-planting",
                daysAfterPlanting = 0,
                npkRatio = NPK(recommendedN * 0.5, recommendedP, recommendedK * 0.5),
                quantity = area * 25,
                instructions = "Apply before planting and incorporate into soil"
            ))
            schedule.add(ApplicationPhase(
                phase = "Mid-season",
                daysAfterPlanting = 30,
                npkRatio = NPK(recommendedN * 0.5, 0.0, recommendedK * 0.5),
                quantity = area * 25,
                instructions = "Side-dress application around plants"
            ))
        }
        
        return FertilizerRecommendation(
            id = UUID.randomUUID().toString(),
            userId = userId,
            cropType = cropType,
            soilType = soilType,
            area = area,
            currentNPK = currentNPK,
            targetYield = targetYield,
            recommendedNPK = NPK(recommendedN, recommendedP, recommendedK),
            fertilizerProducts = products,
            applicationSchedule = schedule,
            estimatedCost = estimatedCost,
            organicAlternatives = organicAlts,
            additionalNotes = additionalNotes.trim().ifEmpty { null }
        )
    }

    /**
     * Convert FertilizerRecommendation to Map for Firestore
     */
    private fun fertilizerRecommendationToMap(recommendation: FertilizerRecommendation): Map<String, Any> {
        return mapOf(
            "id" to recommendation.id,
            "userId" to recommendation.userId,
            "cropType" to recommendation.cropType,
            "soilType" to recommendation.soilType,
            "area" to recommendation.area,
            "currentNPK" to mapOf(
                "nitrogen" to recommendation.currentNPK.nitrogen,
                "phosphorus" to recommendation.currentNPK.phosphorus,
                "potassium" to recommendation.currentNPK.potassium
            ),
            "targetYield" to (recommendation.targetYield ?: 0.0),
            "recommendedNPK" to mapOf(
                "nitrogen" to recommendation.recommendedNPK.nitrogen,
                "phosphorus" to recommendation.recommendedNPK.phosphorus,
                "potassium" to recommendation.recommendedNPK.potassium
            ),
            "fertilizerProducts" to recommendation.fertilizerProducts.map { product ->
                mapOf(
                    "name" to product.name,
                    "npkRatio" to mapOf(
                        "nitrogen" to product.npkRatio.nitrogen,
                        "phosphorus" to product.npkRatio.phosphorus,
                        "potassium" to product.npkRatio.potassium
                    ),
                    "quantityNeeded" to product.quantityNeeded,
                    "pricePerKg" to product.pricePerKg,
                    "totalCost" to product.totalCost,
                    "applicationMethod" to product.applicationMethod,
                    "isOrganic" to product.isOrganic
                )
            },
            "applicationSchedule" to recommendation.applicationSchedule.map { phase ->
                mapOf(
                    "phase" to phase.phase,
                    "daysAfterPlanting" to phase.daysAfterPlanting,
                    "npkRatio" to mapOf(
                        "nitrogen" to phase.npkRatio.nitrogen,
                        "phosphorus" to phase.npkRatio.phosphorus,
                        "potassium" to phase.npkRatio.potassium
                    ),
                    "quantity" to phase.quantity,
                    "instructions" to phase.instructions
                )
            },
            "estimatedCost" to recommendation.estimatedCost,
            "organicAlternatives" to recommendation.organicAlternatives,
            "additionalNotes" to (recommendation.additionalNotes ?: ""),
            "timestamp" to recommendation.timestamp
        )
    }

    /**
     * Convert Firestore Map to FertilizerRecommendation
     */
    @Suppress("UNCHECKED_CAST")
    private fun mapToFertilizerRecommendation(data: Map<String, Any>): FertilizerRecommendation {
        val currentNPKMap = data["currentNPK"] as? Map<String, Any> ?: emptyMap()
        val recommendedNPKMap = data["recommendedNPK"] as? Map<String, Any> ?: emptyMap()
        val productsData = data["fertilizerProducts"] as? List<Map<String, Any>> ?: emptyList()
        val scheduleData = data["applicationSchedule"] as? List<Map<String, Any>> ?: emptyList()
        
        return FertilizerRecommendation(
            id = data["id"] as? String ?: "",
            userId = data["userId"] as? String ?: "",
            cropType = data["cropType"] as? String ?: "",
            soilType = data["soilType"] as? String ?: "",
            area = (data["area"] as? Number)?.toDouble() ?: 0.0,
            currentNPK = NPK(
                nitrogen = (currentNPKMap["nitrogen"] as? Number)?.toDouble() ?: 0.0,
                phosphorus = (currentNPKMap["phosphorus"] as? Number)?.toDouble() ?: 0.0,
                potassium = (currentNPKMap["potassium"] as? Number)?.toDouble() ?: 0.0
            ),
            targetYield = (data["targetYield"] as? Number)?.toDouble(),
            recommendedNPK = NPK(
                nitrogen = (recommendedNPKMap["nitrogen"] as? Number)?.toDouble() ?: 0.0,
                phosphorus = (recommendedNPKMap["phosphorus"] as? Number)?.toDouble() ?: 0.0,
                potassium = (recommendedNPKMap["potassium"] as? Number)?.toDouble() ?: 0.0
            ),
            fertilizerProducts = productsData.map { productMap ->
                val npkMap = productMap["npkRatio"] as? Map<String, Any> ?: emptyMap()
                FertilizerProduct(
                    name = productMap["name"] as? String ?: "",
                    npkRatio = NPK(
                        nitrogen = (npkMap["nitrogen"] as? Number)?.toDouble() ?: 0.0,
                        phosphorus = (npkMap["phosphorus"] as? Number)?.toDouble() ?: 0.0,
                        potassium = (npkMap["potassium"] as? Number)?.toDouble() ?: 0.0
                    ),
                    quantityNeeded = (productMap["quantityNeeded"] as? Number)?.toDouble() ?: 0.0,
                    pricePerKg = (productMap["pricePerKg"] as? Number)?.toDouble() ?: 0.0,
                    totalCost = (productMap["totalCost"] as? Number)?.toDouble() ?: 0.0,
                    applicationMethod = productMap["applicationMethod"] as? String ?: "",
                    isOrganic = productMap["isOrganic"] as? Boolean ?: false
                )
            },
            applicationSchedule = scheduleData.map { phaseMap ->
                val npkMap = phaseMap["npkRatio"] as? Map<String, Any> ?: emptyMap()
                ApplicationPhase(
                    phase = phaseMap["phase"] as? String ?: "",
                    daysAfterPlanting = (phaseMap["daysAfterPlanting"] as? Number)?.toInt() ?: 0,
                    npkRatio = NPK(
                        nitrogen = (npkMap["nitrogen"] as? Number)?.toDouble() ?: 0.0,
                        phosphorus = (npkMap["phosphorus"] as? Number)?.toDouble() ?: 0.0,
                        potassium = (npkMap["potassium"] as? Number)?.toDouble() ?: 0.0
                    ),
                    quantity = (phaseMap["quantity"] as? Number)?.toDouble() ?: 0.0,
                    instructions = phaseMap["instructions"] as? String ?: ""
                )
            },
            estimatedCost = (data["estimatedCost"] as? Number)?.toDouble() ?: 0.0,
            organicAlternatives = (data["organicAlternatives"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            additionalNotes = data["additionalNotes"] as? String,
            timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
        )
    }
}
