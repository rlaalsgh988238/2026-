package com.tourdataproject.tourdata_remote.di

import com.tourdataproject.dataSource.TourDataSource
import com.tourdataproject.tourdata_remote.api.TourApiService
import com.tourdataproject.tourdata_remote.api.factory.TourApiFactory
import com.tourdataproject.tourdata_remote.impl.TourDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TourDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindTourDataSource(
        impl: TourDataSourceImpl
    ): TourDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object TourApiModule {

    @Singleton
    @Provides
    @Named("TourApi")
    fun provideTourRetrofit(): Retrofit {
        return TourApiFactory.createRetrofit()
    }

    @Singleton
    @Provides
    fun provideTourApiService(
        @Named("TourApi") retrofit: Retrofit
    ): TourApiService {
        return retrofit.create(TourApiService::class.java)
    }
}