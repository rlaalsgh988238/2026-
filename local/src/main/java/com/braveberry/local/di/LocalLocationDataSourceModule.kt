package com.braveberry.local.di

import com.braveberry.local.impl.LocationLocalDataSourceImpl
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocalLocationDataSourceModule {

    @Binds
    @Singleton
    internal abstract fun bindLocationLocalDataSource(
        impl: LocationLocalDataSourceImpl
    ): LocationLocalDataSource
}