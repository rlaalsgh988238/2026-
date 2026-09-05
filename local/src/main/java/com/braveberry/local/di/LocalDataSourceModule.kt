package com.braveberry.local.di

import com.braveberry.local.impl.*
import com.braveberry.system_data.dataSource.SystemDatasource
import com.braveberry.toilet_data.dataSource.ToiletDataSource
import com.braveberry.toilet_data.course_data.datasource.CourseDataSource
import com.tourdataproject.map_data.datasource.RegionLocalDataSource
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocalDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindToiletLocalDataSource(impl: ToiletDBDataSourceImpl): ToiletDataSource

    @Binds
    @Singleton
    abstract fun bindRegionLocalDataSource(impl: RegionDataSourceImpl): RegionLocalDataSource

    @Binds
    @Singleton
    abstract fun bindSystemDataSource(impl: SystemDataSourceImpl): SystemDatasource

    @Binds
    @Singleton
    abstract fun bindCourseDataSource(impl: CourseDataSourceImpl): CourseDataSource

    @Binds
    @Singleton
    abstract fun bindLocationLocalDataSource(impl: LocationLocalDataSourceImpl): LocationLocalDataSource
}