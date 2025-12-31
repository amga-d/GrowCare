package com.example.growCare.data.mapper

import com.example.growCare.data.local.database.entity.CropDataEntity
import com.example.growCare.domain.model.CropData
import com.example.growCare.domain.model.CropStage
import com.example.growCare.domain.model.HealthStatus
import javax.inject.Inject

/**
 * Mapper to convert between CropData domain model and CropDataEntity
 */
class CropDataMapper @Inject constructor() {
    
    /**
     * Convert domain model to entity
     */
    fun toEntity(crop: CropData): CropDataEntity {
        return CropDataEntity(
            id = crop.id,
            userId = crop.userId,
            cropName = crop.cropName,
            cropType = crop.cropType,
            variety = crop.variety,
            area = crop.area,
            plantedDate = crop.plantedDate,
            expectedHarvestDate = crop.expectedHarvestDate,
            actualHarvestDate = crop.actualHarvestDate,
            soilType = crop.soilType,
            irrigationType = crop.irrigationType,
            currentStage = crop.currentStage.name,
            healthStatus = crop.healthStatus.name,
            imageUrl = crop.imageUrl,
            notes = crop.notes,
            createdAt = crop.createdAt,
            updatedAt = crop.updatedAt
        )
    }
    
    /**
     * Convert entity to domain model
     */
    fun toDomain(entity: CropDataEntity): CropData {
        return CropData(
            id = entity.id,
            userId = entity.userId,
            cropName = entity.cropName,
            cropType = entity.cropType,
            variety = entity.variety,
            area = entity.area,
            plantedDate = entity.plantedDate,
            expectedHarvestDate = entity.expectedHarvestDate,
            actualHarvestDate = entity.actualHarvestDate,
            soilType = entity.soilType,
            irrigationType = entity.irrigationType,
            currentStage = CropStage.valueOf(entity.currentStage),
            healthStatus = HealthStatus.valueOf(entity.healthStatus),
            imageUrl = entity.imageUrl,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * Convert list of entities to domain models
     */
    fun toDomainList(entities: List<CropDataEntity>): List<CropData> {
        return entities.map { toDomain(it) }
    }
    
    /**
     * Convert list of domain models to entities
     */
    fun toEntityList(crops: List<CropData>): List<CropDataEntity> {
        return crops.map { toEntity(it) }
    }
}
