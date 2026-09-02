package com.braveberry.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.braveberry.local.model.region.RegionDataLocalModel
import com.braveberry.local.model.toilet.ToiletDataLocalModel
import com.braveberry.local.roomDB.AppDatabase
import com.braveberry.local.roomDB.dao.RegionDataDao
import com.braveberry.local.roomDB.dao.ToiletDataDao
import com.braveberry.local.roomDB.dataLoader.regionDataLoader.initRegionTableFromCsv
import com.braveberry.local.roomDB.dataLoader.toiletDataLoader.initToiletTableFromCsv
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var toiletDao: ToiletDataDao
    private lateinit var regionDao: RegionDataDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        toiletDao = db.toiletDao()
        regionDao = db.regionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // ==========================================
    // 화장실 데이터 테스트 (ToiletDataDao)
    // ==========================================

    private fun createDummyToilet(id: Int, name: String, lat: Double, lng: Double): ToiletDataLocalModel {
        return ToiletDataLocalModel(
            id = id, toiletName = name, roadAddress = "테스트 주소", lotAddress = null,
            isUnisex = false, maleToiletBowlCount = 1, maleUrinalCount = 1, maleDisabledToiletCount = 0,
            maleDisabledUrinalCount = 0, maleChildToiletCount = 0, maleChildUrinalCount = 0,
            femaleToiletBowlCount = 1, femaleDisabledToiletCount = 0, femaleChildToiletCount = 0,
            managingAgency = "관리소", phoneNumber = "000", openTime = "24시간",
            latitude = lat, longitude = lng, emergencyBellExists = false, cctvExists = false,
            diaperChangingStationExists = false, updateDate = "2024-01-01"
        )
    }

    @Test
    fun 화장실_단일조회_및_전체조회_테스트() = runBlocking {
        // Given: getToiletData, getAllToiletData 테스트
        val dummy1 = createDummyToilet(1, "화장실A", 37.0, 127.0)
        val dummy2 = createDummyToilet(2, "화장실B", 37.1, 127.1)
        toiletDao.insert(dummy1)
        toiletDao.insert(dummy2)

        // When
        val singleData = toiletDao.getToiletData("1")
        val allData = toiletDao.getAllToiletData()

        // Then
        assertEquals("화장실A", singleData?.toiletName)
        assertEquals(2, allData.size)
    }

    @Test
    fun 화장실_바운딩박스_검색_테스트() = runBlocking {
        // Given: getToiletsInBox 테스트
        val dummy1 = createDummyToilet(1, "영역 밖 화장실(남서)", 35.0, 126.0)
        val dummy2 = createDummyToilet(2, "영역 안 화장실1", 37.5, 127.5)
        val dummy3 = createDummyToilet(3, "영역 안 화장실2", 37.6, 127.6)
        val dummy4 = createDummyToilet(4, "영역 밖 화장실(북동)", 39.0, 129.0)

        toiletDao.insert(dummy1)
        toiletDao.insert(dummy2)
        toiletDao.insert(dummy3)
        toiletDao.insert(dummy4)

        // When: 위도 37.0~38.0, 경도 127.0~128.0 사이 검색
        val boxResult = toiletDao.getToiletsInBox(37.0, 38.0, 127.0, 128.0)

        // Then: 영역 안 화장실 2개만 나와야 함
        assertEquals(2, boxResult.size)
        assertTrue(boxResult.any { it.toiletName == "영역 안 화장실1" })
        assertTrue(boxResult.any { it.toiletName == "영역 안 화장실2" })
    }

    @Test
    fun 화장실_이름_유사도_검색_테스트() = runBlocking {
        // Given: getSimilarNameToiletData 테스트 (우선순위 정렬 확인)
        val dummy1 = createDummyToilet(1, "경기 가평 화장실", 37.0, 127.0) // 중간 단어 시작 (2순위)
        val dummy2 = createDummyToilet(2, "가평 터미널 화장실", 37.0, 127.0) // 이름으로 바로 시작 (1순위)
        val dummy3 = createDummyToilet(3, "서울 화장실", 37.0, 127.0) // 검색 안 됨

        toiletDao.insert(dummy1)
        toiletDao.insert(dummy2)
        toiletDao.insert(dummy3)

        // When: "가평"으로 검색
        val searchResult = toiletDao.getSimilarNameToiletData("가평")

        // Then: 2개가 검색되어야 하며, "가평 터미널 화장실"이 1순위로 나와야 함
        assertEquals(2, searchResult.size)
        assertEquals("가평 터미널 화장실", searchResult[0].toiletName)
        assertEquals("경기 가평 화장실", searchResult[1].toiletName)
    }

    @Test
    fun 화장실_CSV_파일_파싱_및_초기화_테스트() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        initToiletTableFromCsv(targetContext, toiletDao)
        assertTrue(toiletDao.getAllToiletData().isNotEmpty())
    }

    // ==========================================
    // 법정동 지역 데이터 테스트 (RegionDataDao)
    // ==========================================

    @Test
    fun 지역_단일조회_및_인기도시_조회_테스트() = runBlocking {
        // Given: getAnyRegion, getPopularRegions 테스트
        val dummyRegions = listOf(
            RegionDataLocalModel("1100000000", "서울특별시", null, null, null, isPopular = true),
            RegionDataLocalModel("4111000000", "경기도", "수원시", null, null, isPopular = true),
            RegionDataLocalModel("5200000000", "전북특별자치도", "정읍시", null, null, isPopular = false)
        )
        regionDao.insertList(dummyRegions)

        // When
        val anyRegion = regionDao.getAnyRegion()
        val popularRegions = regionDao.getPopularRegions()

        // Then
        assertNotNull(anyRegion)
        assertEquals(2, popularRegions.size)
    }

    @Test
    fun 지역_실시간_검색_읍면동_제외_테스트() = runBlocking {
        // Given: searchRegions 테스트
        val dummyRegions = listOf(
            RegionDataLocalModel("4831000000", "경상남도", "거제시", null, null, isPopular = false),
            RegionDataLocalModel("4831011000", "경상남도", "거제시", "장승포동", null, isPopular = false)
        )
        regionDao.insertList(dummyRegions)

        // When
        val searchResults = regionDao.searchRegions("거제")

        // Then: town이 null인 "거제시" 1개만 검색되어야 함
        assertEquals(1, searchResults.size)
        assertEquals("거제시", searchResults.first().city)
        assertEquals(null, searchResults.first().town)
    }

    @Test
    fun 지역_인기도_업데이트_Code_테스트() = runBlocking {
        // Given: updatePopularityByCode 테스트
        val dummy = RegionDataLocalModel("5200000000", "전북특별자치도", "정읍시", null, null, isPopular = false)
        regionDao.insert(dummy)

        // When: 코드로 업데이트
        regionDao.updatePopularityByCode("5200000000", true)
        val popularRegions = regionDao.getPopularRegions()

        // Then
        assertEquals(1, popularRegions.size)
        assertEquals("정읍시", popularRegions.first().city)
    }

    @Test
    fun 지역_인기도_업데이트_Name_테스트() = runBlocking {
        // Given: updatePopularityByName 테스트
        val dummy = RegionDataLocalModel("5200000000", "전북특별자치도", "정읍시", null, null, isPopular = false)
        regionDao.insert(dummy)

        // When: 이름으로 업데이트
        regionDao.updatePopularityByName("정읍시", true)
        val popularRegions = regionDao.getPopularRegions()

        // Then
        assertEquals(1, popularRegions.size)
        assertEquals("정읍시", popularRegions.first().city)
    }

    // ==========================================
    // 법정동 지역 데이터 통합 테스트 (실제 CSV 기반)
    // ==========================================

    @Test
    fun 실제_데이터_기반_초기_인기도시_세팅_확인_테스트() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        initRegionTableFromCsv(targetContext, regionDao)

        val popularRegions = regionDao.getPopularRegions()
        assertEquals("정확히 12개의 인기 도시가 세팅되어야 합니다.", 12, popularRegions.size)
    }

    @Test
    fun 실제_데이터_기반_인기도시_업데이트_테스트() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        initRegionTableFromCsv(targetContext, regionDao)

        val initialPopularRegions = regionDao.getPopularRegions()
        assertEquals(12, initialPopularRegions.size)

        // "포항"을 인기 도시로 업데이트
        regionDao.updatePopularityByName("포항시", true)

        val updatedPopularRegions = regionDao.getPopularRegions()
        assertEquals("포항시가 추가되어 13개가 되어야 합니다.", 13, updatedPopularRegions.size)
        assertTrue("포항시가 인기 도시에 포함되어야 합니다.", updatedPopularRegions.any { it.city == "포항시" })
    }

    @Test
    fun 실제_데이터_기반_실시간_검색_테스트() = runBlocking {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        initRegionTableFromCsv(targetContext, regionDao)

        // "거제"로 검색
        val searchResults = regionDao.searchRegions("거제")

        assertTrue("검색 결과가 존재해야 합니다.", searchResults.isNotEmpty())
        assertTrue("검색 결과에 읍/면/동 단위가 포함되어서는 안 됩니다.", searchResults.all { it.town == null && it.village == null })
        assertTrue("검색 결과에 거제시가 포함되어야 합니다.", searchResults.any { it.city == "거제시" })
    }
    @Test
    fun 화장실_데이터_샘플_출력_테스트() = runBlocking {
        // Given: 실제 CSV 데이터를 파싱하여 DB에 삽입
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        initToiletTableFromCsv(targetContext, toiletDao)

        // When: 전체 데이터를 가져와서 셔플 후 50개 추출
        val allData = toiletDao.getAllToiletData()
        val sampleData = allData.shuffled().take(50)

        // Then: 로그 출력
        println("==== 화장실 데이터 샘플 50개 출력 시작 (총 개수: ${allData.size}) ====")
        sampleData.forEachIndexed { index, toilet ->
            val logMessage = """
                [샘플 ${index + 1}]
                ID: ${toilet.id}
                이름: ${toilet.toiletName}
                주소(도로명): ${toilet.roadAddress}
                주소(지번): ${toilet.lotAddress}
                좌표: 위도(${toilet.latitude}), 경도(${toilet.longitude})
                남성용(대/소): ${toilet.maleToiletBowlCount} / ${toilet.maleUrinalCount}
                여성용(대): ${toilet.femaleToiletBowlCount}
                안전시설: 비상벨(${if (toilet.emergencyBellExists) "Y" else "N"}), CCTV(${if (toilet.cctvExists) "Y" else "N"})
                ------------------------------------------------------------
            """.trimIndent()

            // 테스트 결과창(System.out)과 Logcat 모두에서 확인 가능하도록 함
            println(logMessage)
            android.util.Log.d("ToiletSample", logMessage)
        }

        assertTrue("데이터가 최소 50개 이상은 있어야 샘플링이 의미가 있습니다.", allData.size >= 50)
    }
}
