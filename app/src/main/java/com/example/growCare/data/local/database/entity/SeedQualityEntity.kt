package com.example.growCare.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.growCare.data.local.database.converter.StringListConverter

/**
 * Room entity for storing seed quality analysis results locally
 * Maps to the SeedQuality domain model
 */
@Entity(tableName = "seed_analyses")
@TypeConverters(StringListConverter::class)
data class SeedQualityEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val seedType: String,
    val imageUrl: String,
    val qualityScore: Int,
    val sizeAssessment: String, // Stored as String, converted from enum
    val colorConsistency: String, // Stored as String, converted from enum
    val damagePercentage: Int,
    val damageTypes: List<String>, // List of DamageType enum names
    val germinationPotential: Int,
    val recommendations: List<String>,
    val storageAdvice: String?,
    val isRecommendedForUse: Boolean,
    val timestamp: Long
)
