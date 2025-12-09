package com.example.growCare.domain.model

/**
 * Domain model representing seed quality analysis results
 */
data class SeedQuality(
    val id: String,
    val userId: String,
    val seedType: String,
    val imageUrl: String,
    val qualityScore: Int, // 0-100
    val sizeAssessment: SeedSize,
    val colorConsistency: ColorConsistency,
    val damagePercentage: Int, // 0-100
    val damageTypes: List<DamageType>,
    val germinationPotential: Int, // 0-100
    val recommendations: List<String>,
    val storageAdvice: String? = null,
    val isRecommendedForUse: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SeedSize {
    SMALL,
    MEDIUM,
    LARGE,
    MIXED
}

enum class ColorConsistency {
    UNIFORM,
    SLIGHTLY_VARIED,
    HIGHLY_VARIED
}

enum class DamageType {
    NONE,
    INSECT,
    FUNGAL,
    MECHANICAL,
    MOLD,
    DISCOLORATION,
    SHRIVELED
}

/**
 * Helper function to get quality rating text
 */
fun Int.toQualityRating(): String {
    return when {
        this >= 90 -> "Excellent"
        this >= 75 -> "Good"
        this >= 60 -> "Fair"
        this >= 40 -> "Poor"
        else -> "Very Poor"
    }
}

/**
 * Helper function to get germination rating
 */
fun Int.toGerminationRating(): String {
    return when {
        this >= 85 -> "High"
        this >= 70 -> "Moderate"
        this >= 50 -> "Low"
        else -> "Very Low"
    }
}
