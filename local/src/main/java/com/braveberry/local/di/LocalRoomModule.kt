package com.braveberry.local.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.braveberry.local.roomDB.AppDatabase
import com.braveberry.local.roomDB.DatabaseRegistrationManager
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
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        registrationManager: DatabaseRegistrationManager
    ): AppDatabase {
        Log.d("TOUR_DATA_DEBUG", "1. buildDatabase called")
        val db = AppDatabase.buildDatabase(context, registrationManager)

        // 🌟 중요: DB를 강제로 오픈시킵니다.
        // 이 코드가 실행되는 순간 Room의 Callback(onCreate/onOpen)이 트리거됩니다.
        db.openHelper.writableDatabase

        return db
    }


    // 모든 DAO 제공을 여기서 관리
    @Provides
    @Singleton
    fun provideToiletDao(database: AppDatabase): ToiletDataDao = database.toiletDao()

    @Provides
    @Singleton
    fun provideRegionDataDao(database: AppDatabase): RegionDataDao = database.regionDao()
}

