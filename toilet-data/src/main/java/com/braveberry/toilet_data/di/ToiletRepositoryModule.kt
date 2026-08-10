package com.braveberry.toilet_data.di

import com.braveberry.toilet_data.impl.ToiletRepositoryImpl
import com.tourdataproject.domain.repository.ToiletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ToiletRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindToiletRepository(repo: ToiletRepositoryImpl): ToiletRepository
}