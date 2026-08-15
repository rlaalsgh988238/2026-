package com.tourdataproject.map_remote.di

import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_remote.impl.KakaoMapRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class KakaoMapDataSourceModule {

    @Binds
    @Singleton
    // 파라미터로 구현체(Impl)를 받고, 반환 타입으로 인터페이스를 명시합니다.
    abstract fun bindKakaoMapRemoteDataSource(
        impl: KakaoMapRemoteDataSourceImpl
    ): KakaoMapRemoteDataSource
}