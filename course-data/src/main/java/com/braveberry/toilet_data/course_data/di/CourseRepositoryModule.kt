package com.braveberry.toilet_data.course_data.di

import com.braveberry.toilet_data.course_data.datasource.CourseDataSource
import com.braveberry.toilet_data.course_data.impl.CourseRepositoryImpl
import com.tourdataproject.domain.repository.CourseRepository
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Module

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CourseRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        courseRepositoryImpl: CourseRepositoryImpl
    ): CourseRepository
}