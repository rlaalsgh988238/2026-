package com.braveberry.local.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.braveberry.local.roomDB.AppDatabase
import com.braveberry.local.roomDB.RoomConstant
import com.braveberry.local.roomDB.dao.CourseDao
import com.braveberry.local.roomDB.dao.RegionDataDao
import com.braveberry.local.roomDB.dao.ToiletDataDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocalRoomModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.buildDatabase(context)
    }

    // 모든 DAO 제공을 여기서 관리
    @Provides
    @Singleton
    fun provideToiletDao(database: AppDatabase): ToiletDataDao = database.toiletDao()

    @Provides
    @Singleton
    fun provideRegionDataDao(database: AppDatabase): RegionDataDao = database.regionDao()
}

