package com.braveberry.localDB.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ToiletLocal::class], version = 1, exportSchema = false)
internal abstract class ToiletDatabase : RoomDatabase() {
    abstract fun toiletDao(): ToiletDao
}