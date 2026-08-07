package com.braveberry.localDB

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braveberry.localDB.impl.ToiletDBDataSourceImpl
import com.braveberry.localDB.roomDB.ToiletDatabase
import com.braveberry.toilet_data.model.ToiletEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToiletDBDataSourceImplTest {

    private lateinit var db: ToiletDatabase
    private lateinit var dataSource: ToiletDBDataSourceImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context, ToiletDatabase::class.java
        ).build()

        val dao = db.toiletDao()
        dataSource = ToiletDBDataSourceImpl(dao)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetToiletDataTest() = runBlocking {
        val dummyEntity = ToiletEntity(
            id = 1,
            toiletName = "임플 테스트 화장실",
            roadAddress = "테스트 도로명",
            lotAddress = "테스트 지번",
            isUnisex = false,
            maleToiletBowlCount = 1,
            maleUrinalCount = 1,
            maleDisabledToiletCount = 0,
            maleDisabledUrinalCount = 0,
            maleChildToiletCount = 0,
            maleChildUrinalCount = 0,
            femaleToiletBowlCount = 1,
            femaleDisabledToiletCount = 0,
            femaleChildToiletCount = 0,
            managingAgency = "테스트 기관",
            phoneNumber = "010-0000-0000",
            openTime = "24시간",
            latitude = 37.0,
            longitude = 127.0,
            emergencyBellExists = false,
            cctvExists = false,
            diaperChangingStationExists = false,
            updateDate = "2024-01-01"
        )

        dataSource.insertToiletData(dummyEntity)

        val result = dataSource.getToiletData("1")

        // 로그 출력: 단일 데이터 확인
        println("조회된 데이터: $result")

        assertEquals(dummyEntity.toiletName, result?.toiletName)
    }

    @Test
    fun insertListAndGetAllTest() = runBlocking {
        val dummyList = listOf(
            ToiletEntity(
                id = 1, toiletName = "화장실 A", roadAddress = "", lotAddress = "",
                isUnisex = false, maleToiletBowlCount = 0, maleUrinalCount = 0,
                maleDisabledToiletCount = 0, maleDisabledUrinalCount = 0, maleChildToiletCount = 0,
                maleChildUrinalCount = 0, femaleToiletBowlCount = 0, femaleDisabledToiletCount = 0,
                femaleChildToiletCount = 0, managingAgency = "", phoneNumber = "", openTime = "",
                latitude = 0.0, longitude = 0.0, emergencyBellExists = false, cctvExists = false,
                diaperChangingStationExists = false, updateDate = ""
            ),
            ToiletEntity(
                id = 2, toiletName = "화장실 B", roadAddress = "", lotAddress = "",
                isUnisex = false, maleToiletBowlCount = 0, maleUrinalCount = 0,
                maleDisabledToiletCount = 0, maleDisabledUrinalCount = 0, maleChildToiletCount = 0,
                maleChildUrinalCount = 0, femaleToiletBowlCount = 0, femaleDisabledToiletCount = 0,
                femaleChildToiletCount = 0, managingAgency = "", phoneNumber = "", openTime = "",
                latitude = 0.0, longitude = 0.0, emergencyBellExists = false, cctvExists = false,
                diaperChangingStationExists = false, updateDate = ""
            )
        )

        dataSource.insertToiletDataList(dummyList)

        val resultList = dataSource.getAllToiletData()

        // 로그 출력: 리스트 개수 및 각 항목 확인
        println("전체 조회 결과 개수: ${resultList.size}")
        resultList.forEach { println("조회된 항목: ${it.toiletName}") }

        assertEquals(2, resultList.size)
        assertEquals("화장실 A", resultList[0].toiletName)
        assertEquals("화장실 B", resultList[1].toiletName)
    }
}