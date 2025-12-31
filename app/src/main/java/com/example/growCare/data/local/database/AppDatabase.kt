package com.example.growCare.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.growCare.data.local.database.converter.StringListConverter
import com.example.growCare.data.local.database.dao.*
import com.example.growCare.data.local.database.entity.*

/**
 * Room Database for GrowCare application
 * 
 * This is the main database configuration class that defines:
 * - All entities (tables)
 * - Database version
 * - Type converters
 * - DAO accessors
 * 
 * Version History:
 * - Version 1: Initial database schema with all core entities
 * - Version 2: Added imageUrl field to ChatMessageEntity
 */
@Database(
    entities = [
        ChatMessageEntity::class,
        CropDataEntity::class,
        DiseaseAnalysisEntity::class,
        SeedQualityEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Provides access to chat message operations
     */
    abstract fun chatDao(): ChatDao
    
    /**
     * Provides access to crop data operations
     */
    abstract fun cropDao(): CropDao
    
    /**
     * Provides access to disease analysis operations
     */
    abstract fun diseaseAnalysisDao(): DiseaseAnalysisDao
    
    /**
     * Provides access to seed analysis operations
     */
    abstract fun seedAnalysisDao(): SeedAnalysisDao
    
    /**
     * Provides access to user data operations
     */
    abstract fun userDao(): UserDao
    
    companion object {
        const val DATABASE_NAME = "growcare_database"
    }
}
