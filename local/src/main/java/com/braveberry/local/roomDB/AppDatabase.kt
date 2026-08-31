package com.braveberry.local.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.braveberry.local.model.course.CourseLocalModel
import com.braveberry.local.model.region.RegionDataLocalModel
import com.braveberry.local.model.toilet.ToiletDataLocalModel
import com.braveberry.local.roomDB.dao.CourseDao
import com.braveberry.local.roomDB.dao.RegionDataDao
import com.braveberry.local.roomDB.dao.ToiletDataDao
import com.braveberry.local.roomDB.dataLoader.regionDataLoader.initRegionTableFromCsv
import com.braveberry.local.roomDB.dataLoader.toiletDataLoader.initToiletTableFromCsv
import com.braveberry.local.util.CourseTypeConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    companion object {
        private const val DATABASE_NAME = "tour_data.db"

        fun buildDatabase(context: Context): AppDatabase {
            lateinit var database: AppDatabase

            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // 데이터베이스가 생성된 후 별도 스코프에서 초기화 진행
                    CoroutineScope(Dispatchers.IO).launch {
                        // 생성된 database 인스턴스에서 직접 dao를 가져옵니다.
                        initRegionTableFromCsv(context, database.regionDao())
                        initToiletTableFromCsv(context, database.toiletDao())
                    }
                }
            }).build()

            return database
        }
    }
}