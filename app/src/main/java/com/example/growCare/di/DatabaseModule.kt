package com.example.growCare.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database dependency injection module
 * Provides Room database and DAO instances
 * 
 * Note: AppDatabase and DAOs will be created in Phase 5
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    // TODO: Uncomment when AppDatabase is created
    /*
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "growcare_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCropDao(database: AppDatabase): CropDao {
        return database.cropDao()
    }
    
    @Provides
    @Singleton
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    */
}
