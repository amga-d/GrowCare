package com.example.growCare.data.mapper

import com.example.growCare.data.local.database.entity.SeedQualityEntity
import com.example.growCare.domain.model.*
import javax.inject.Inject

/**
 * Mapper to convert between SeedQuality domain model and SeedQualityEntity
 */
class SeedQualityMapper @Inject constructor() {
    
    /**
     * Convert domain model to entity
     */
    fun toEntity(seedQuality: SeedQuality): SeedQualityEntity {
        return SeedQualityEntity(
            id = seedQuality.id,
            userId = seedQuality.userId,
            seedType = seedQuality.seedType,
            imageUrl = seedQuality.imageUrl,
            qualityScore = seedQuality.qualityScore,
            sizeAssessment = seedQuality.sizeAssessment.name,
            colorConsistency = seedQuality.colorConsistency.name,
            damagePercentage = seedQuality.damagePercentage,
            damageTypes = seedQuality.damageTypes.map { it.name },
            germinationPotential = seedQuality.germinationPotential,
            recommendations = seedQuality.recommendations,
            storageAdvice = seedQuality.storageAdvice,
            isRecommendedForUse = seedQuality.isRecommendedForUse,
            timestamp = seedQuality.timestamp
        )
    }
    
    /**
     * Convert entity to domain model
     */
    fun toDomain(entity: SeedQualityEntity): SeedQuality {
        return SeedQuality(
            id = entity.id,
            userId = entity.userId,
            seedType = entity.seedType,
            imageUrl = entity.imageUrl,
            qualityScore = entity.qualityScore,
            sizeAssessment = SeedSize.valueOf(entity.sizeAssessment),
            colorConsistency = ColorConsistency.valueOf(entity.colorConsistency),
            damagePercentage = entity.damagePercentage,
            damageTypes = entity.damageTypes.map { DamageType.valueOf(it) },
            germinationPotential = entity.germinationPotential,
            recommendations = entity.recommendations,
            storageAdvice = entity.storageAdvice,
            isRecommendedForUse = entity.isRecommendedForUse,
            timestamp = entity.timestamp
        )
    }
    
    /**
     * Convert list of entities to domain models
     */
    fun toDomainList(entities: List<SeedQualityEntity>): List<SeedQuality> {
        return entities.map { toDomain(it) }
    }
    
    /**
     * Convert list of domain models to entities
     */
    fun toEntityList(seedQualities: List<SeedQuality>): List<SeedQualityEntity> {
        return seedQualities.map { toEntity(it) }
    }
}
