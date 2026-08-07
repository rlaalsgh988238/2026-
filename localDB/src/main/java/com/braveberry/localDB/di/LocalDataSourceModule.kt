package com.braveberry.localDB.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.braveberry.localDB.impl.ToiletDBDataSourceImpl
import com.braveberry.toilet_data.localDB.ToiletDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocalDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindToiletLocalDataSource(source: ToiletDBDataSourceImpl): ToiletDataSource
}