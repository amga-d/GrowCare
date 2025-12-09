package com.example.growCare.di

import dagger.Module
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository dependency injection module
 * Binds repository interfaces to their implementations
 * 
 * Note: Repository interfaces and implementations will be created in Phase 4-5
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    // TODO: Uncomment when repositories are implemented
    
    /*
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository
    
    @Binds
    @Singleton
    abstract fun bindCropRepository(
        impl: CropRepositoryImpl
    ): CropRepository
    
    @Binds
    @Singleton
    abstract fun bindDiseaseRepository(
        impl: DiseaseRepositoryImpl
    ): DiseaseRepository
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository
    */
}
