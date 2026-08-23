package com.braveberry.local.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.braveberry.local.model.CourseLocalModel
import com.braveberry.local.model.ToiletDataLocalModel
import com.braveberry.local.roomDB.dao.CourseDao
import com.braveberry.local.roomDB.dao.ToiletDataDao
import com.braveberry.local.util.CourseTypeConverter

//TODO: 이거 이름 ToiletDatabase-> AppDatabase 어떰 Course도 여기서 쓸 것 같은데 ( 앱 내 DB 싱글톤 위해)
@Database(entities = [ToiletDataLocalModel::class, CourseLocalModel::class], version = RoomConstant.ROOM_VERSION)
@TypeConverters(CourseTypeConverter::class)
internal abstract class ToiletDatabase : RoomDatabase() {
    abstract fun toiletDao(): ToiletDataDao
    abstract fun courseDao(): CourseDao
}