package com.tourdataproject.map_data.di
import dagger.Module
import com.tourdataproject.domain.repository.MapRepository
import com.tourdataproject.map_data.repositoryImpl.MapRepositoryImpl
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class KakaoMapRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindKakaoMapRepository(impl: MapRepositoryImpl): MapRepository
}