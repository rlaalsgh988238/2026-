package com.braveberry.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.braveberry.local.model.toilet.ToiletDataLocalModel
import com.braveberry.local.roomDB.AppDatabase
import com.braveberry.local.roomDB.dao.ToiletDataDao
import com.braveberry.local.roomDB.toiletDataLoader.initToiletTableFromCsv
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    // 1. db 변수 주석 해제
    private lateinit var db: AppDatabase
    private lateinit var dao: ToiletDataDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()

        // 2. 작성하신 Database 클래스의 함수명(toiletDao)에 맞게 수정
        dao = db.toiletDao()
    }

    @After
    fun closeDb() {
        // 3. 테스트 종료 후 DB 닫기 주석 해제
        db.close()
    }

    @Test
    fun insertAndGetToiletData() = runBlocking {
        val dummyData = ToiletDataLocalModel(
            id = 1,
            toiletName = "테스트 화장실",
            roadAddress = "서울시 테스트구 테스트동",
            lotAddress = null,
            isUnisex = false,
            maleToiletBowlCount = 2,
            maleUrinalCount = 3,
            maleDisabledToiletCount = 1,
            maleDisabledUrinalCount = 0,
            maleChildToiletCount = 0,
            maleChildUrinalCount = 0,
            femaleToiletBowlCount = 5,
            femaleDisabledToiletCount = 1,
            femaleChildToiletCount = 0,
            managingAgency = "테스트 관리소",
            phoneNumber = "02-123-4567",
            openTime = "24시간",
            latitude = 37.123456,
            longitude = 127.123456,
            emergencyBellExists = true,
            cctvExists = true,
            diaperChangingStationExists = false,
            updateDate = "2024-01-01"
        )

        dao.insert(dummyData)

        // DAO의 getToiletData 파라미터가 String이므로 "1"로 전달
        val loadedData = dao.getToiletData("1")

        assertEquals(dummyData.toiletName, loadedData?.toiletName)
        assertEquals(dummyData.latitude, loadedData?.latitude)
    }

    @Test
    fun testInitToiletTableFromCsv() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        initToiletTableFromCsv(targetContext, dao)

        val allData = dao.getAllToiletData()

        assertTrue("CSV 파일 파싱 후 DB에 데이터가 존재해야 합니다.", allData.isNotEmpty())
        println("첫 번째 데이터 확인: ${allData.first()}")
    }
}
