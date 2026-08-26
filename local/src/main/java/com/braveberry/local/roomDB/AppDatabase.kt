package com.braveberry.local.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.braveberry.local.model.course.CourseLocalModel
import com.braveberry.local.model.region.RegionDataLocalModel
import com.braveberry.local.model.toilet.ToiletDataLocalModel
import com.braveberry.local.roomDB.dao.CourseDao
import com.braveberry.local.roomDB.dao.RegionDataDao
import com.braveberry.local.roomDB.dao.ToiletDataDao
import com.braveberry.local.util.CourseTypeConverter

@Database(
    entities = [
        ToiletDataLocalModel::class,
        CourseLocalModel::class,
        RegionDataLocalModel::class
    ],
    version = RoomConstant.ROOM_VERSION
)
@TypeConverters(CourseTypeConverter::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun toiletDao(): ToiletDataDao
    abstract fun courseDao(): CourseDao
    abstract fun regionDao(): RegionDataDao
}