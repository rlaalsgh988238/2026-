package com.braveberry.local.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.braveberry.local.model.ToiletDataLocalModel
import com.braveberry.local.roomDB.dao.ToiletDataDao

@Database(entities = [ToiletDataLocalModel::class], version = RoomConstant.ROOM_VERSION)
internal abstract class ToiletDatabase : RoomDatabase() {
    abstract fun toiletDao(): ToiletDataDao
}