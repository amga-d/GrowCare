package com.example.growCare.domain.model

/**
 * Domain model representing fertilizer recommendation
 */
data class FertilizerRecommendation(
    val id: String,
    val userId: String,
    val cropType: String,
    val soilType: String,
    val area: Double, // in acres
    val currentNPK: NPK,
    val targetYield: Double? = null,
    val recommendedNPK: NPK,
    val fertilizerProducts: List<FertilizerProduct>,
    val applicationSchedule: List<ApplicationPhase>,
    val estimatedCost: Double,
    val organicAlternatives: List<String> = emptyList(),
    val additionalNotes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * NPK (Nitrogen, Phosphorus, Potassium) values
 */
data class NPK(
    val nitrogen: Double, // N value
    val phosphorus: Double, // P value
    val potassium: Double // K value
) {
    /**
     * Format as NPK ratio string (e.g., "10-20-10")
     */
    fun toRatioString(): String {
        return "$nitrogen-$phosphorus-$potassium"
    }
    
    /**
     * Calculate total NPK
     */
    fun total(): Double {
        return nitrogen + phosphorus + potassium
    }
}

/**
 * Fertilizer product recommendation
 */
data class FertilizerProduct(
    val name: String,
    val npkRatio: NPK,
    val quantityNeeded: Double, // in kg
    val pricePerKg: Double,
    val totalCost: Double,
    val applicationMethod: String,
    val isOrganic: Boolean = false
)

/**
 * Application schedule for fertilizer
 */
data class ApplicationPhase(
    val phase: String, // e.g., "Pre-planting", "Vegetative", "Flowering"
    val daysAfterPlanting: Int,
    val npkRatio: NPK,
    val quantity: Double, // in kg
    val instructions: String
)

/**
 * Soil types commonly used
 */
enum class SoilType {
    SANDY,
    CLAY,
    LOAMY,
    SILTY,
    PEATY,
    CHALKY
}

/**
 * Extension function to get soil properties
 */
fun SoilType.getProperties(): String {
    return when (this) {
        SoilType.SANDY -> "Well-drained, low nutrients, warms quickly"
        SoilType.CLAY -> "Heavy, retains water, rich in nutrients"
        SoilType.LOAMY -> "Ideal balance, fertile, well-drained"
        SoilType.SILTY -> "Moisture-retentive, fertile, light"
        SoilType.PEATY -> "High organic matter, acidic, moisture-retentive"
        SoilType.CHALKY -> "Alkaline, stony, free-draining"
    }
}
