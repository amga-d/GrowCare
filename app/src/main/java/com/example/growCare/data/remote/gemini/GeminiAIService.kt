package com.example.growCare.data.remote.gemini

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.growCare.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with Gemini AI API
 * Handles plant disease detection and seed quality analysis
 */
@Singleton
class GeminiAIService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    /**
     * Analyze plant image for disease detection
     * Returns JSON with disease information
     */
    suspend fun analyzePlantDisease(imageUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: throw Exception("Failed to load image")

            val prompt = """
                Analyze this plant image for diseases and health issues. Provide a detailed analysis in JSON format.
                
                Return ONLY valid JSON (no markdown formatting) with this exact structure:
                {
                  "diseaseName": "name of the disease or 'Healthy' if no disease detected",
                  "confidence": 85,
                  "severity": "MILD" or "MODERATE" or "SEVERE",
                  "symptoms": ["symptom 1", "symptom 2", "symptom 3"],
                  "treatment": ["treatment step 1", "treatment step 2", "treatment step 3"],
                  "prevention": ["prevention measure 1", "prevention measure 2"],
                  "additionalNotes": "any additional observations or recommendations"
                }
                
                Guidelines:
                - confidence should be 0-100 (your confidence in the diagnosis)
                - severity: MILD (early stage), MODERATE (progressing), SEVERE (advanced)
                - symptoms: observable signs on the plant
                - treatment: specific actionable steps to treat the disease
                - prevention: measures to prevent future occurrences
                - Be specific and practical for farmers
                
                Return ONLY the JSON object, no additional text.
            """.trimIndent()

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = model.generateContent(inputContent)
            val responseText = response.text ?: throw Exception("Empty response from AI")
            
            // Clean response - remove markdown code blocks if present
            val cleanedResponse = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            // Validate JSON
            JSONObject(cleanedResponse)
            
            Result.success(cleanedResponse)
        } catch (e: Exception) {
            Result.failure(Exception("Disease analysis failed: ${e.message}"))
        }
    }

    /**
     * Analyze seed quality from image
     * Returns JSON with quality assessment
     */
    suspend fun analyzeSeedQuality(imageUri: Uri, seedType: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: throw Exception("Failed to load image")

            val prompt = """
                Analyze this image of $seedType seeds for quality assessment. Provide a detailed analysis in JSON format.
                
                Return ONLY valid JSON (no markdown formatting) with this exact structure:
                {
                  "qualityScore": 85,
                  "sizeAssessment": "SMALL" or "MEDIUM" or "LARGE" or "MIXED",
                  "colorConsistency": "UNIFORM" or "SLIGHTLY_VARIED" or "HIGHLY_VARIED",
                  "damagePercentage": 10,
                  "damageTypes": ["NONE" or "INSECT", "FUNGAL", "MECHANICAL", "MOLD", "DISCOLORATION", "SHRIVELED"],
                  "germinationPotential": 90,
                  "recommendations": ["recommendation 1", "recommendation 2", "recommendation 3"],
                  "storageAdvice": "optimal storage conditions and duration",
                  "isRecommendedForUse": true
                }
                
                Guidelines:
                - qualityScore: 0-100 (overall quality assessment)
                - sizeAssessment: uniformity and appropriateness of seed size
                - colorConsistency: how uniform the seed color is
                - damagePercentage: 0-100 (percentage of seeds showing damage)
                - damageTypes: list all observed damage types (use "NONE" if no damage)
                - germinationPotential: 0-100 (likelihood of successful germination)
                - recommendations: practical advice for farmers
                - storageAdvice: how to store these seeds properly
                - isRecommendedForUse: true if quality score >= 60, false otherwise
                
                Be specific and practical for agricultural use.
                Return ONLY the JSON object, no additional text.
            """.trimIndent()

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = model.generateContent(inputContent)
            val responseText = response.text ?: throw Exception("Empty response from AI")
            
            // Clean response - remove markdown code blocks if present
            val cleanedResponse = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            // Validate JSON
            JSONObject(cleanedResponse)
            
            Result.success(cleanedResponse)
        } catch (e: Exception) {
            Result.failure(Exception("Seed quality analysis failed: ${e.message}"))
        }
    }

    /**
     * Parse disease analysis JSON into structured data
     */
    fun parseDiseaseAnalysisJson(json: String): DiseaseAnalysisDto {
        val jsonObject = JSONObject(json)
        
        return DiseaseAnalysisDto(
            diseaseName = jsonObject.getString("diseaseName"),
            confidence = jsonObject.getInt("confidence"),
            severity = jsonObject.getString("severity"),
            symptoms = jsonObject.getJSONArray("symptoms").toStringList(),
            treatment = jsonObject.getJSONArray("treatment").toStringList(),
            prevention = jsonObject.getJSONArray("prevention").toStringList(),
            additionalNotes = jsonObject.optString("additionalNotes", null)
        )
    }

    /**
     * Parse seed quality JSON into structured data
     */
    fun parseSeedQualityJson(json: String): SeedQualityDto {
        val jsonObject = JSONObject(json)
        
        return SeedQualityDto(
            qualityScore = jsonObject.getInt("qualityScore"),
            sizeAssessment = jsonObject.getString("sizeAssessment"),
            colorConsistency = jsonObject.getString("colorConsistency"),
            damagePercentage = jsonObject.getInt("damagePercentage"),
            damageTypes = jsonObject.getJSONArray("damageTypes").toStringList(),
            germinationPotential = jsonObject.getInt("germinationPotential"),
            recommendations = jsonObject.getJSONArray("recommendations").toStringList(),
            storageAdvice = jsonObject.optString("storageAdvice", null),
            isRecommendedForUse = jsonObject.getBoolean("isRecommendedForUse")
        )
    }

    private fun JSONArray.toStringList(): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until length()) {
            list.add(getString(i))
        }
        return list
    }
}

/**
 * DTO for disease analysis response
 */
data class DiseaseAnalysisDto(
    val diseaseName: String,
    val confidence: Int,
    val severity: String,
    val symptoms: List<String>,
    val treatment: List<String>,
    val prevention: List<String>,
    val additionalNotes: String?
)

/**
 * DTO for seed quality response
 */
data class SeedQualityDto(
    val qualityScore: Int,
    val sizeAssessment: String,
    val colorConsistency: String,
    val damagePercentage: Int,
    val damageTypes: List<String>,
    val germinationPotential: Int,
    val recommendations: List<String>,
    val storageAdvice: String?,
    val isRecommendedForUse: Boolean
)
