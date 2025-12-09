package com.example.growCare.domain.model

/**
 * Domain model representing crop data and cultivation information
 */
data class CropData(
    val id: String,
    val userId: String,
    val cropName: String,
    val cropType: String, // e.g., "Vegetable", "Grain", "Fruit"
    val variety: String? = null,
    val area: Double, // in acres
    val plantedDate: Long,
    val expectedHarvestDate: Long,
    val actualHarvestDate: Long? = null,
    val soilType: String,
    val irrigationType: String, // e.g., "Drip", "Sprinkler", "Flood"
    val currentStage: CropStage = CropStage.PLANTED,
    val healthStatus: HealthStatus = HealthStatus.HEALTHY,
    val imageUrl: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CropStage {
    PLANTED,
    GERMINATION,
    VEGETATIVE,
    FLOWERING,
    FRUITING,
    RIPENING,
    HARVESTED
}

enum class HealthStatus {
    HEALTHY,
    AT_RISK,
    DISEASED,
    CRITICAL
}
