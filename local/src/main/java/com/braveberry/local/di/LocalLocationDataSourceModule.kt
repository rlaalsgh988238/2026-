package com.braveberry.local.di

import com.braveberry.local.impl.LocationLocalDataSourceImpl
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import com.tourdataproject.map_data.repositoryImpl.KakaoMapRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalLocationDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindLocationLocalDataSource(
        impl: LocationLocalDataSourceImpl
    ): LocationLocalDataSource
}