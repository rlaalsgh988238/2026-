package com.tourdataproject.domain.usecase.calculateAceesibility.usecase_test

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.model.Region
import com.tourdataproject.domain.model.Toilet
import com.tourdataproject.domain.repository.MapRepository
import com.tourdataproject.domain.repository.SystemRepository
import com.tourdataproject.domain.repository.ToiletRepository
import com.tourdataproject.domain.usecase.getToIlet.GetToiletsByDistanceUseCase
import com.tourdataproject.domain.usecase.getToIlet.GetToiletsByIdUseCase
import com.tourdataproject.domain.usecase.getToIlet.GetToiletsByNameUseCase
import com.tourdataproject.domain.usecase.plan.GetPopularCitiesUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionByKeywordUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.domain.usecase.splash.CheckDatabaseInitUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UseCaseTests {

    // Repositories (Mock)
    private lateinit var mapRepository: MapRepository
    private lateinit var systemRepository: SystemRepository
    private lateinit var toiletRepository: ToiletRepository

    // UseCases
    private lateinit var getPopularCitiesUseCase: GetPopularCitiesUseCase
    private lateinit var getRegionByKeywordUseCase: GetRegionByKeywordUseCase
    private lateinit var getRegionPositionUseCase: GetRegionPositionUseCase
    private lateinit var checkDatabaseInitUseCase: CheckDatabaseInitUseCase
    private lateinit var getToiletsByDistanceUseCase: GetToiletsByDistanceUseCase
    private lateinit var getToiletsByIdUseCase: GetToiletsByIdUseCase
    private lateinit var getToiletsByNameUseCase: GetToiletsByNameUseCase

    @Before
    fun setUp() {
        mapRepository = mockk()
        systemRepository = mockk()
        toiletRepository = mockk()

        getPopularCitiesUseCase = GetPopularCitiesUseCase(mapRepository)
        getRegionByKeywordUseCase = GetRegionByKeywordUseCase(mapRepository)
        getRegionPositionUseCase = GetRegionPositionUseCase(mapRepository)
        checkDatabaseInitUseCase = CheckDatabaseInitUseCase(systemRepository)
        getToiletsByDistanceUseCase = GetToiletsByDistanceUseCase(toiletRepository)
        getToiletsByIdUseCase = GetToiletsByIdUseCase(toiletRepository)
        getToiletsByNameUseCase = GetToiletsByNameUseCase(toiletRepository)
    }

    @Test
    fun `인기 도시 목록을 성공적으로 가져온다`() = runTest {
        // Given
        val mockRegions = listOf(
            Region(code = "1100000000", province = "서울특별시", city = null, town = null, village = null, isPopular = true)
        )
        every { mapRepository.getPopularCity() } returns flowOf(DataResource.Success(mockRegions))

        // When
        val result = getPopularCitiesUseCase().first()

        // Then
        assertTrue(result is DataResource.Success)
        assertEquals(mockRegions, (result as DataResource.Success).data)
        verify(exactly = 1) { mapRepository.getPopularCity() }
    }

    @Test
    fun `키워드로 지역을 성공적으로 검색한다`() = runTest {
        // Given
        val keyword = "여수"
        val mockRegions = listOf(
            Region(code = "4613000000", province = "전라남도", city = "여수시", town = null, village = null, isPopular = false)
        )
        every { mapRepository.getRegionByKeyword(keyword) } returns flowOf(DataResource.Success(mockRegions))

        // When
        val result = getRegionByKeywordUseCase(keyword).first()

        // Then
        assertTrue(result is DataResource.Success)
        assertEquals(mockRegions, (result as DataResource.Success).data)
        verify(exactly = 1) { mapRepository.getRegionByKeyword(keyword) }
    }

    @Test
    fun `지역 이름으로 위치 좌표를 성공적으로 가져온다`() = runTest {
        // Given
        val regionName = "서울특별시"
        val mockLocation = Location(latitude = 37.5665, longitude = 126.9780)
        every { mapRepository.getRegionPosition(regionName) } returns flowOf(DataResource.Success(mockLocation))

        // When
        val result = getRegionPositionUseCase(regionName).first()

        // Then
        assertTrue(result is DataResource.Success)
        assertEquals(mockLocation, (result as DataResource.Success).data)
        verify(exactly = 1) { mapRepository.getRegionPosition(regionName) }
    }

    @Test
    fun `데이터베이스 초기화 상태를 성공적으로 확인한다`() = runTest {
        // Given
        every { systemRepository.isDatabaseInit() } returns flowOf(DataResource.Success(Unit))

        // When
        val result = checkDatabaseInitUseCase().first()

        // Then
        assertTrue(result is DataResource.Success)
        verify(exactly = 1) { systemRepository.isDatabaseInit() }
    }

    @Test
    fun `거리 기반으로 화장실 목록을 성공적으로 가져온다`() = runTest {
        // Given
        val distance = 1000f
        val lat = 37.5665
        val lng = 126.9780
        val mockToilets = listOf(mockk<Toilet>())

        // 올바른 순서(lat, lng)로 모킹
        every { toiletRepository.getToiletsByDistance(distance, lat, lng) } returns flowOf(DataResource.Success(mockToilets))

        // When
        val result = getToiletsByDistanceUseCase(distance, lat, lng).first()

        // Then
        assertTrue(result is DataResource.Success)
        assertEquals(mockToilets, (result as DataResource.Success).data)

        // 올바른 순서(lat, lng)로 검증
        verify(exactly = 1) { toiletRepository.getToiletsByDistance(distance, lat, lng) }
    }



    @Test
    fun `ID로 특정 화장실 정보를 성공적으로 가져온다`() = runTest {
        // Given
        val toiletId = "1"
        val mockToilet = mockk<Toilet>()
        every { toiletRepository.getToiletById(toiletId) } returns flowOf(DataResource.Success(mockToilet))

        // When
        val result = getToiletsByIdUseCase(toiletId).first()

        // Then
        assertTrue(result is DataResource.Success)
        assertEquals(mockToilet, (result as DataResource.Success).data)
        verify(exactly = 1) { toiletRepository.getToiletById(toiletId) }
    }

    @Test
    fun `이름으로 화장실 목록을 성공적으로 검색한다`() = runTest {
        // Given
        val toiletName = "공중화장실"
        val mockToilets = listOf(mockk<Toilet>())
        every { toiletRepository.getToiletsByName(toiletName) } returns flowOf(DataResource.Success(mockToilets))

        // When
        val result = getToiletsByNameUseCase(toiletName).first()

        // Then
        assertTrue(result is DataResource.Success)
        assertEquals(mockToilets, (result as DataResource.Success).data)
        verify(exactly = 1) { toiletRepository.getToiletsByName(toiletName) }
    }

    @Test
    fun `에러 발생 시 Error Resource를 반환한다`() = runTest {
        // Given
        val exception = RuntimeException("Network Error")
        every { mapRepository.getPopularCity() } returns flowOf(DataResource.Error(exception))

        // When
        val result = getPopularCitiesUseCase().first()

        // Then
        assertTrue(result is DataResource.Error)
        assertEquals(exception, (result as DataResource.Error).throwable)
    }
}
