package com.braveberry.localDB.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.braveberry.localDB.roomDB.ToiletDatabase
import com.braveberry.localDB.roomDB.RoomConstant
import com.braveberry.localDB.roomDB.dao.ToiletDataDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object LocalRoomModule {

    @Provides
    @Singleton
    fun provideToiletDatabase(@ApplicationContext context: Context): ToiletDatabase =
        Room.databaseBuilder(
            context,
            ToiletDatabase::class.java,
            RoomConstant.DB_NAME
        ).build()

    @Provides
    @Singleton
    fun provideToiletDao(database: ToiletDatabase): ToiletDataDao = database.toiletDao()
}
