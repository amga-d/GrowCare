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
                You are an expert agricultural seed analyst with 20+ years of experience. Analyze this image of $seedType seeds for comprehensive quality assessment.
                
                SEED-TYPE-SPECIFIC EVALUATION CRITERIA:
                
                For WHEAT/BARLEY: Good quality seeds are plump, uniform in size, golden/amber color, intact kernel surface, no shriveling.
                For RICE: Look for translucent appearance, uniform white/cream color, no chalky white spots, elongated shape, no cracks.
                For CORN/MAIZE: Kernels should be hard, yellow/orange color, smooth surface, no blue/green mold, uniform size.
                For BEANS/LEGUMES: Smooth coat, uniform color, no insect holes, kidney/oval shape retained, not wrinkled.
                For SUNFLOWER: Black/striped shells intact, plump appearance, no hollow seeds, uniform size, no oil leakage stains.
                For TOMATO: Flattened disc shape, cream/tan color, fuzzy coat intact, no fungal spots.
                For LETTUCE: Tiny elongated seeds, light tan/brown, no clumping, dry texture.
                
                MULTI-FACTOR QUALITY SCORING SYSTEM (Total = 100):
                1. UNIFORMITY (30 points): Size consistency across batch, minimal mixed sizes, standard deviation in dimensions
                2. DAMAGE ASSESSMENT (25 points): Physical damage, insect holes, cracks, mold spots, discoloration
                3. COLOR CONSISTENCY (20 points): Uniform natural color for seed type, no bleaching/darkening, no contamination
                4. SIZE ADEQUACY (15 points): Seeds meet size standards for variety, not undersized/oversized
                5. PURITY (10 points): No foreign material, weed seeds, broken pieces, chaff
                
                DAMAGE TYPE IDENTIFICATION GUIDE:
                - INSECT: Visible holes, tunnels, exit holes (usually round, 1-3mm)
                - FUNGAL: Discolored patches (black/white/green), fuzzy growth, shriveled appearance
                - MECHANICAL: Cracks, splits, broken pieces, crushed kernels
                - MOLD: Fuzzy surface growth, musty appearance, greenish tint
                - DISCOLORATION: Unnatural darkening/lightening, water damage stains
                - SHRIVELED: Wrinkled surface, reduced volume, lightweight appearance
                - NONE: If seeds appear healthy with no visible damage
                
                GERMINATION POTENTIAL INDICATORS:
                - HIGH (80-100%): Plump, firm, uniform color, intact seed coat, proper moisture content
                - MEDIUM (60-79%): Minor discoloration, slight size variation, some mechanical damage
                - LOW (40-59%): Visible fungal spots, insect damage, severe shriveling, poor color
                - VERY LOW (<40%): Extensive damage, mold coverage, hollow/lightweight, broken pieces
                
                QUALITY GRADE THRESHOLDS for $seedType:
                - EXCELLENT (90-100): <5% damage, uniform size, optimal color, 95%+ germination potential
                - VERY GOOD (80-89): 5-10% damage, mostly uniform, good color, 85-94% germination
                - GOOD (70-79): 10-15% damage, acceptable uniformity, 75-84% germination
                - FAIR (60-69): 15-25% damage, mixed sizes, 65-74% germination
                - POOR (<60): >25% damage, high variation, <65% germination - NOT RECOMMENDED
                
                Return ONLY valid JSON (no markdown formatting) with this exact structure:
                {
                  "qualityScore": 85,
                  "sizeAssessment": "SMALL" or "MEDIUM" or "LARGE" or "MIXED",
                  "colorConsistency": "UNIFORM" or "SLIGHTLY_VARIED" or "HIGHLY_VARIED",
                  "damagePercentage": 10,
                  "damageTypes": ["NONE" or "INSECT", "FUNGAL", "MECHANICAL", "MOLD", "DISCOLORATION", "SHRIVELED"],
                  "germinationPotential": 90,
                  "recommendations": ["specific recommendation 1", "specific recommendation 2", "specific recommendation 3"],
                  "storageAdvice": "optimal storage conditions and duration for this seed type",
                  "isRecommendedForUse": true
                }
                
                ANALYSIS REQUIREMENTS:
                1. Examine MULTIPLE seeds in the image, not just one
                2. Calculate damagePercentage based on proportion of damaged seeds visible
                3. Consider seed type norms when scoring (don't compare rice to corn standards)
                4. Be realistic - most farmer seeds are 60-80 quality, not 90+
                5. Provide actionable recommendations specific to observed issues
                6. Factor in: uniformity (30%), damage (25%), color (20%), size (15%), purity (10%)
                7. If quality score is below 60, set isRecommendedForUse to false
                8. Germination potential should correlate with quality score and damage
                
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
