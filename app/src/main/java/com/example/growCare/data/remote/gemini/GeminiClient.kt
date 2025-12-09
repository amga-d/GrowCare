package com.example.growCare.data.remote.gemini

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.growCare.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for Google Gemini AI integration
 * Handles text generation, image analysis, and chat functionality
 */
@Singleton
class GeminiClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MODEL_NAME = "gemini-1.5-flash"
        
        // Prompts for different features
        private const val DISEASE_ANALYSIS_PROMPT = """
            You are an expert agricultural AI assistant specializing in plant disease diagnosis.
            Analyze this plant image carefully and provide:
            
            1. **Disease Identification**: Name the disease if detected (or state "Healthy" if no disease)
            2. **Confidence Level**: Your confidence in this diagnosis (0-100%)
            3. **Symptoms Observed**: List visible symptoms in the image
            4. **Severity**: Rate as Mild, Moderate, or Severe
            5. **Treatment Recommendations**: Specific steps to treat this disease
            6. **Prevention Measures**: How to prevent this disease in the future
            7. **Additional Notes**: Any other relevant observations
            
            Format your response clearly with these headers. Be specific and practical in your recommendations.
        """
        
        private const val SEED_QUALITY_PROMPT = """
            You are an expert agricultural AI assistant specializing in seed quality assessment.
            Analyze this seed image carefully and provide:
            
            1. **Quality Score**: Overall quality rating (0-100)
            2. **Size Assessment**: Are seeds uniform in size? (Small/Medium/Large/Mixed)
            3. **Color Consistency**: Is color uniform? (Uniform/Slightly Varied/Highly Varied)
            4. **Damage Assessment**: Percentage of damaged seeds and types of damage
            5. **Germination Potential**: Estimated germination rate (0-100%)
            6. **Recommendations**: Should these seeds be used? Any treatments needed?
            7. **Storage Advice**: How to properly store these seeds
            
            Format your response clearly with these headers. Be specific in your assessment.
        """
        
        private const val CHAT_SYSTEM_PROMPT = """
            You are an expert agricultural AI assistant helping farmers with:
            - Crop management and cultivation advice
            - Pest and disease identification and treatment
            - Fertilizer recommendations
            - Irrigation and water management
            - Soil health and improvement
            - Weather-based farming decisions
            - Sustainable farming practices
            
            Provide practical, actionable advice. Keep responses clear and concise.
            Use simple language that farmers can easily understand.
            When uncertain, acknowledge it and suggest consulting local agricultural experts.
        """
    }

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    // ============ Text Generation ============

    /**
     * Send a simple text message and get response
     */
    suspend fun sendMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(message)
            val text = response.text ?: "No response generated"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send a message with system context (for chat)
     */
    suspend fun sendChatMessage(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fullPrompt = "$CHAT_SYSTEM_PROMPT\n\nUser: $userMessage\nAssistant:"
            val response = generativeModel.generateContent(fullPrompt)
            val text = response.text ?: "No response generated"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send a chat message and get streaming response
     */
    fun sendChatMessageStream(userMessage: String): Flow<String> = flow {
        try {
            val fullPrompt = "$CHAT_SYSTEM_PROMPT\n\nUser: $userMessage\nAssistant:"
            generativeModel.generateContentStream(fullPrompt).collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            emit("Error: ${e.message}")
        }
    }

    // ============ Image Analysis ============

    /**
     * Analyze image with custom prompt
     */
    suspend fun analyzeImage(
        imageUri: Uri,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmapFromUri(imageUri)
            
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val text = response.text ?: "No analysis available"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze plant disease from image
     */
    suspend fun analyzePlantDisease(imageUri: Uri): Result<String> = 
        analyzeImage(imageUri, DISEASE_ANALYSIS_PROMPT)

    /**
     * Analyze seed quality from image
     */
    suspend fun analyzeSeedQuality(imageUri: Uri): Result<String> = 
        analyzeImage(imageUri, SEED_QUALITY_PROMPT)

    /**
     * Analyze crop health with custom question
     */
    suspend fun analyzeCropHealth(
        imageUri: Uri,
        question: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmapFromUri(imageUri)
            
            val prompt = """
                You are an agricultural expert. The farmer has this question about their crop:
                
                "$question"
                
                Analyze the image and provide a detailed answer to their question.
            """.trimIndent()
            
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val text = response.text ?: "No analysis available"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ Multi-turn Chat ============

    /**
     * Start a chat session with history
     * This allows for contextual conversations
     */
    suspend fun startChatWithHistory(
        history: List<Pair<String, String>>, // List of (user message, AI response) pairs
        newMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Build chat history
            val chatHistory = history.map { (userMsg, aiMsg) ->
                listOf(
                    content(role = "user") { text(userMsg) },
                    content(role = "model") { text(aiMsg) }
                )
            }.flatten()

            val chat = generativeModel.startChat(history = chatHistory)
            val response = chat.sendMessage(newMessage)
            val text = response.text ?: "No response generated"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Start a chat session with history and get streaming response
     */
    fun startChatWithHistoryStream(
        history: List<Pair<String, String>>,
        newMessage: String
    ): Flow<String> = flow {
        try {
            // Build chat history
            val chatHistory = history.map { (userMsg, aiMsg) ->
                listOf(
                    content(role = "user") { text(userMsg) },
                    content(role = "model") { text(aiMsg) }
                )
            }.flatten()

            val chat = generativeModel.startChat(history = chatHistory)
            chat.sendMessageStream(newMessage).collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            emit("Error: ${e.message}")
        }
    }

    // ============ Specialized Agricultural Queries ============

    /**
     * Get fertilizer recommendation based on crop and soil
     */
    suspend fun getFertilizerRecommendation(
        cropType: String,
        soilType: String,
        area: Double,
        currentNPK: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                As an agricultural expert, provide detailed fertilizer recommendations for:
                
                - Crop: $cropType
                - Soil Type: $soilType
                - Area: $area acres
                - Current NPK levels: $currentNPK
                
                Please provide:
                1. Recommended NPK ratio
                2. Quantity needed
                3. Application schedule
                4. Expected cost range
                5. Organic alternatives
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: "No recommendation available"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get weather-based farming advice
     */
    suspend fun getWeatherBasedAdvice(
        weather: String,
        cropType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Current weather conditions: $weather
                Crop: $cropType
                
                As an agricultural expert, provide:
                1. Immediate actions needed based on this weather
                2. Irrigation recommendations
                3. Pest/disease risks in this weather
                4. Any protective measures needed
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: "No advice available"
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ Helper Methods ============

    /**
     * Load bitmap from URI
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    /**
     * Check if API key is configured
     */
    fun isConfigured(): Boolean {
        return BuildConfig.GEMINI_API_KEY.isNotEmpty()
    }
}
