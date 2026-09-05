package com.braveberry.local.di

import android.content.Context
import com.braveberry.local.roomDB.AppDatabase
import com.braveberry.local.roomDB.DatabaseRegistrationManager
import com.braveberry.local.roomDB.dao.CourseDao
import com.braveberry.local.roomDB.dao.RegionDataDao
import com.braveberry.local.roomDB.dao.ToiletDataDao
import com.braveberry.local.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocalDatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        registrationManager: DatabaseRegistrationManager
    ): AppDatabase {
        Log.d("TOUR_DATA_DEBUG", "1. buildDatabase called")
        val db = AppDatabase.buildDatabase(context, registrationManager)
        db.openHelper.writableDatabase // DB 강제 오픈
        return db
    }

    @Provides
    @Singleton
    fun provideToiletDao(database: AppDatabase): ToiletDataDao = database.toiletDao()

    @Provides
    @Singleton
    fun provideRegionDataDao(database: AppDatabase): RegionDataDao = database.regionDao()

    @Provides
    @Singleton
    fun provideCourseDao(database: AppDatabase): CourseDao = database.courseDao()
}