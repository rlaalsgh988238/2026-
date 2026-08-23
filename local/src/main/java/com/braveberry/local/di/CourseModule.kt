package com.braveberry.local.di

import com.braveberry.local.impl.CourseDataSourceImpl
import com.braveberry.local.roomDB.AppDatabase
import com.braveberry.local.roomDB.dao.CourseDao
import com.braveberry.toilet_data.course_data.datasource.CourseDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object CourseLocalProvideModule {

    @Provides
    @Singleton
    fun provideCourseDao(database: AppDatabase): CourseDao {
        return database.courseDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CourseLocalBindModule {

    @Binds
    @Singleton
    abstract fun bindCourseDataSource(
        impl: CourseDataSourceImpl
    ): CourseDataSource
}