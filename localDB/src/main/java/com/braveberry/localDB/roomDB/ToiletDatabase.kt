package com.braveberry.localDB.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.braveberry.localDB.model.ToiletDataLocal
import com.braveberry.localDB.roomDB.dao.ToiletDataDao

@Database(entities = [ToiletDataLocal::class], version = RoomConstant.ROOM_VERSION)
internal abstract class ToiletDatabase : RoomDatabase() {
    abstract fun toiletDao(): ToiletDataDao
}