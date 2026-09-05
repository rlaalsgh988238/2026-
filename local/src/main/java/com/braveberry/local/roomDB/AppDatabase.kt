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
import com.braveberry.local.util.Log
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

    // AppDatabase.kt 내부
    companion object {
        fun buildDatabase(
            context: Context,
            registrationManager: DatabaseRegistrationManager
        ): AppDatabase {
            val dbFile = context.getDatabasePath(RoomConstant.DB_NAME)
            val isFirstRun = !dbFile.exists()

            val database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                RoomConstant.DB_NAME
            ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // onCreate에서는 아무것도 하지 않습니다.
                    // 여기서 비동기를 돌리면 인스턴스 참조 문제가 생길 수 있기 때문입니다.
                }
            }).build()

            // DB 객체 생성이 완료된 후 로직 실행
            if (isFirstRun) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Log.d("TOUR_DATA_DEBUG", "Starting CSV Initial Load...")
                        initRegionTableFromCsv(context, database.regionDao())
                        initToiletTableFromCsv(context, database.toiletDao())

                        registrationManager.markAsReady()
                        Log.d("TOUR_DATA_DEBUG", "CSV Load Success")
                    } catch (e: Exception) {
                        Log.e("TOUR_DATA_DEBUG", "CRITICAL: CSV Load Failed", e)
                        // 여기에 실패 시 재시도 로직이나 에러 처리를 추가할 수 있음
                    }
                }
            } else {
                // 이미 파일이 있다면 즉시 완료
                registrationManager.markAsReady()
            }

            return database
        }
    }
}