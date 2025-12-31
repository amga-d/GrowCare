package com.example.growCare.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing crop data locally
 * Maps to the CropData domain model
 */
@Entity(tableName = "crops")
data class CropDataEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val cropName: String,
    val cropType: String,
    val variety: String?,
    val area: Double,
    val plantedDate: Long,
    val expectedHarvestDate: Long,
    val actualHarvestDate: Long?,
    val soilType: String,
    val irrigationType: String,
    val currentStage: String, // Stored as String, converted from enum
    val healthStatus: String, // Stored as String, converted from enum
    val imageUrl: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)
