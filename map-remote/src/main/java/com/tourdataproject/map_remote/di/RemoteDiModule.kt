package com.tourdataproject.map_remote.di

import com.tourdataproject.map_remote.api.factory.KakaoApiFactory
import com.tourdataproject.map_remote.api.KakaoMapApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteDiModule {

    @Singleton
    @Provides
    @Named("KakaoMap")
    fun provideKakaoRetrofit(): Retrofit {
        return KakaoApiFactory.createRetrofit()
    }

    @Singleton
    @Provides
    fun provideMapApi(
        @Named("KakaoMap") retrofit: Retrofit
    ): KakaoMapApi {
        return retrofit.create(KakaoMapApi::class.java)
    }

}