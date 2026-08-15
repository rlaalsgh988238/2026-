package com.tourdataproject.tourdata_remote.di

import com.tourdataproject.tourdata_remote.api.TourApi
import com.tourdataproject.tourdata_remote.api.factory.TourApiFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TourApiModule {

    @Singleton
    @Provides
    @Named("TourApiRetrofit") // 카카오 맵 Retrofit과 헷갈리지 않게 이름표(Named) 부착
    fun provideTourRetrofit(): Retrofit {
        return TourApiFactory.createRetrofit()
    }

    @Singleton
    @Provides
    fun provideTourApi(
        @Named("TourApiRetrofit") retrofit: Retrofit
    ): TourApi {
        return retrofit.create(TourApi::class.java)
    }
}