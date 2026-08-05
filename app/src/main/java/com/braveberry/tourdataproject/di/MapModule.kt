package com.braveberry.tourdataproject.di

import com.tourdataproject.map_remote.KakaoApiFactory
import com.tourdataproject.map_remote.MapApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton
import kotlin.jvm.java

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
    ): MapApi {
        return retrofit.create(MapApi::class.java)
    }

}
