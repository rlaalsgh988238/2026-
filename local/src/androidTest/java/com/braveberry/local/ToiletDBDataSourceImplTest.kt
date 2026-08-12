package com.braveberry.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braveberry.local.impl.ToiletDBDataSourceImpl
import com.braveberry.local.roomDB.ToiletDatabase
import com.braveberry.local.util.LocationCalculator
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
    private lateinit var locationCalculator: LocationCalculator

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, ToiletDatabase::class.java
        ).allowMainThreadQueries().build()

        locationCalculator = LocationCalculator()
        dataSource = ToiletDBDataSourceImpl(db.toiletDao(), locationCalculator)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `데이터를_삽입하고_ID로_조회하면_동일한_데이터가_반환된다`() = runBlocking {
        // given
        val toilet = createDummyToilet(id = 1, name = "테스트 화장실")
        dataSource.insertToiletData(toilet)

        // when
        val result = dataSource.getToiletData("1")

        // then
        assertEquals(toilet.toiletName, result?.toiletName)
        assertEquals(toilet.id, result?.id)
    }

    @Test
    fun `리스트_형태로_데이터를_삽입하면_전체_조회_시_모두_반환된다`() = runBlocking {
        // given
        val toilets = listOf(
            createDummyToilet(id = 1, name = "화장실 A"),
            createDummyToilet(id = 2, name = "화장실 B")
        )
        dataSource.insertToiletDataList(toilets)

        // when
        val resultList = dataSource.getAllToiletData()

        // then
        assertEquals(2, resultList.size)
        assertEquals("화장실 A", resultList[0].toiletName)
        assertEquals("화장실 B", resultList[1].toiletName)
    }

    @Test
    fun `영역_기반_조회_시_설정된_반경_내의_데이터만_반환된다`() = runBlocking {
        // given
        val centerLat = 37.4979
        val centerLng = 127.0276
        val distance = 1000.0f // 1km

        val inBoxToilet = createDummyToilet(id = 1, name = "범위 안", lat = 37.4980, lng = 127.0277)
        val outOfBoxToilet = createDummyToilet(id = 2, name = "범위 밖", lat = 38.0000, lng = 128.0000)
        dataSource.insertToiletDataList(listOf(inBoxToilet, outOfBoxToilet))

        // when
        val resultList = dataSource.getToiletDataInBox(distance, centerLat, centerLng)

        // then
        assertEquals(1, resultList.size)
        assertEquals("범위 안", resultList[0].toiletName)
    }

    @Test
    fun `이름_검색_시_전방_일치와_단어_시작_데이터가_우선순위에_따라_정렬되어_반환된다`() = runBlocking {
        // given
        val dummyList = listOf(
            createDummyToilet(id = 1, name = "가천대역 화장실"),      // 1순위 (시작)
            createDummyToilet(id = 2, name = "경기 가평군 화장실"),    // 2순위 (공백 후 시작)
            createDummyToilet(id = 3, name = "강가 편의점 화장실"),    // 제외 (중간 포함)
            createDummyToilet(id = 4, name = "가평 공중화장실"),      // 1순위 (시작)
            createDummyToilet(id = 5, name = "서울역 화장실")         // 제외 (무관)
        )
        dataSource.insertToiletDataList(dummyList)

        // when
        val resultList = dataSource.getSimilarNameToiletData("가")

        // then
        // 1. 검색 결과 개수 확인 (가천대역, 가평 공중, 경기 가평)
        assertEquals(3, resultList.size)

        // 2. 정렬 순서 확인: [1순위(가나다순)] -> [2순위]
        assertEquals("가천대역 화장실", resultList[0].toiletName)
        assertEquals("가평 공중화장실", resultList[1].toiletName)
        assertEquals("경기 가평군 화장실", resultList[2].toiletName)

        // 3. 노이즈 데이터 제외 확인
        val hasNoise = resultList.any { it.toiletName == "강가 편의점 화장실" }
        assertEquals(false, hasNoise)
    }

    private fun createDummyToilet(
        id: Int,
        name: String,
        lat: Double = 0.0,
        lng: Double = 0.0
    ): ToiletEntity {
        return ToiletEntity(
            id = id,
            toiletName = name,
            roadAddress = "도로명 주소",
            lotAddress = "지번 주소",
            isUnisex = false,
            maleToiletBowlCount = 0,
            maleUrinalCount = 0,
            maleDisabledToiletCount = 0,
            maleDisabledUrinalCount = 0,
            maleChildToiletCount = 0,
            maleChildUrinalCount = 0,
            femaleToiletBowlCount = 0,
            femaleDisabledToiletCount = 0,
            femaleChildToiletCount = 0,
            managingAgency = "관리기관",
            phoneNumber = "010-0000-0000",
            openTime = "24시간",
            latitude = lat,
            longitude = lng,
            emergencyBellExists = false,
            cctvExists = false,
            diaperChangingStationExists = false,
            updateDate = "2024-01-01"
        )
    }
}
