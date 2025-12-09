package com.example.growCare.di

import dagger.Module
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository dependency injection module
 * Binds repository interfaces to their implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: com.example.growCare.data.repository.AuthRepositoryImpl
    ): com.example.growCare.domain.repository.AuthRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: com.example.growCare.data.repository.WeatherRepositoryImpl
    ): com.example.growCare.domain.repository.WeatherRepository
}
