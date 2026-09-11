package com.tourdataproject.di

import com.tourdataproject.domain.repository.TourRepository
import com.tourdataproject.impl.TourDataRepositoryImpl
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Module

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TourRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTourRepository(
        repo: TourDataRepositoryImpl
    ): TourRepository
}