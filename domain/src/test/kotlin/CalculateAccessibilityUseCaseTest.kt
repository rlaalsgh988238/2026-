package com.tourdataproject.domain.usecase.calculateAceesibility

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Toilet
import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.domain.repository.ToiletRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CalculateAccessibilityUseCaseTest {

    private lateinit var toiletRepository: ToiletRepository
    private lateinit var useCase: CalculateAccessibilityUseCase

    private lateinit var calculator: AccessibilityCalculator
    @Before
    fun setup() {
        // 1. Repository는 DB를 타니까 가짜(Mock)로 만듭니다.
        toiletRepository = mockk(relaxed = true)

        // 2. Calculator는 순수 연산 로직이므로 진짜(Real) 객체로 만듭니다.
        calculator = AccessibilityCalculator()

        // 3. 테스트할 UseCase에 가짜 Repo와 진짜 Calculator를 모두 주입합니다.
        useCase = CalculateAccessibilityUseCase(toiletRepository, calculator)
    }

    @Test
    fun `화장실이 3개 이상이고 거리가 아주 가까우면_GOOD_상태와_100점을_반환한다`() = runTest {
        // Given (준비)
        val lat = 37.500
        val lng = 127.000

        // 위도 0.001도 차이는 약 111미터입니다. (111m / 67m = 약 1.6분 소요)
        val mockToilets = listOf(
            createDummyToilet(id = 1, lat = 37.501, lng = 127.000), // ~111m 거리 (Plan A)
            createDummyToilet(id = 2, lat = 37.502, lng = 127.000), // ~222m 거리 (Plan B)
            createDummyToilet(id = 3, lat = 37.503, lng = 127.000)  // ~333m 거리
        )

        // Repo가 위 3개의 화장실 리스트를 반환하도록 조작합니다.
        coEvery {
            toiletRepository.getToiletsByDistance(1500f, lat, lng)
        } returns flowOf(DataResource.Success(mockToilets))

        // When (실행)
        val result = useCase(lat, lng)

        // Then (검증)
        // Score A (<=5분) -> 100점 * 0.6 = 60
        // Count (3개) -> 100점 * 0.2 = 20
        // Score B (Plan A~B 111m, <=10분) -> 100점 * 0.2 = 20
        // 총합: 100점 -> GOOD
        assertEquals(AccessibilityStatus.GOOD, result.status)
        assertEquals(100, result.safetyScore)
        assertEquals("1", result.planAToiletId)
        assertEquals("2", result.planBToiletId)
    }

    @Test
    fun `화장실이 1개이고 거리가 조금 멀면_WARNING_상태와_40점을_반환한다`() = runTest {
        // Given (준비)
        val lat = 37.500
        val lng = 127.000

        // 위도 0.009도 차이는 약 1,000미터입니다. (1000m / 67m = 약 14.9분 소요)
        val mockToilets = listOf(
            createDummyToilet(id = 1, lat = 37.509, lng = 127.000)
        )

        coEvery {
            toiletRepository.getToiletsByDistance(1500f, lat, lng)
        } returns flowOf(DataResource.Success(mockToilets))

        // When (실행)
        val result = useCase(lat, lng)

        // Then (검증)
        // Score A (<=15분) -> 50점 * 0.6 = 30
        // Count (1개) -> 50점 * 0.2 = 10
        // Score B (Plan B 없음) -> 0점
        // 총합: 40점 -> WARNING
        assertEquals(AccessibilityStatus.WARNING, result.status)
        assertEquals(40, result.safetyScore)
        assertEquals("1", result.planAToiletId)
        assertNull(result.planBToiletId) // Plan B는 null이어야 함
    }

    @Test
    fun `화장실이 아예 없으면_BAD_상태와_0점을_반환한다`() = runTest {
        // Given (준비)
        val lat = 37.500
        val lng = 127.000

        // 텅 빈 리스트 반환
        coEvery {
            toiletRepository.getToiletsByDistance(1500f, lat, lng)
        } returns flowOf(DataResource.Success(emptyList()))

        // When (실행)
        val result = useCase(lat, lng)

        // Then (검증)
        assertEquals(AccessibilityStatus.BAD, result.status)
        assertEquals(0, result.safetyScore)
        assertNull(result.planAToiletId)
        assertNull(result.planBToiletId)
    }

    // --- 테스트용 더미 데이터 생성 도우미 함수 ---
    private fun createDummyToilet(id: Int, lat: Double, lng: Double): Toilet {
        return Toilet(
            id = id,
            toiletName = "테스트 화장실 $id",
            roadAddress = null,
            lotAddress = null,
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
            managingAgency = null,
            phoneNumber = null,
            openTime = null,
            latitude = lat,
            longitude = lng,
            emergencyBellExists = true,
            cctvExists = true,
            diaperChangingStationExists = false,
            updateDate = null
        )
    }
}
