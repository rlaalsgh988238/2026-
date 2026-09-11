package com.tourdataproject.domain.usecase.calculateAceesibility.usecase_test

import com.tourdataproject.domain.usecase.tourData.GetTourDataToiletInfoUseCase

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.domain.repository.TourRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTourDataToiletInfoUseCaseTest {

    private lateinit var mockRepository: TourRepository
    private lateinit var useCase: GetTourDataToiletInfoUseCase

    @Before
    fun setUp() {
        mockRepository = mockk()
        useCase = GetTourDataToiletInfoUseCase(mockRepository)
    }

    @Test
    fun `invoke 실행 시 Repository를 호출하여 무장애 정보 Flow를 그대로 반환한다`() = runTest {
        // Given
        val targetContentId = "12345"
        val mockInfo = AccessibilityInfo(
            status = AccessibilityStatus.GOOD,
            parking = "장애인 주차구역",
            elevator = "엘리베이터",
            safetyScore = 100,
            planAToiletId = null,
            planBToiletId = null,
            route = null, restroom = null, wheelchair = null, exit = null
        )

        every {
            mockRepository.getTourDataToiletInfo(targetContentId)
        } returns flowOf(DataResource.Loading(), DataResource.Success(mockInfo))

        // When
        val emissions = useCase(targetContentId).toList()

        // Then
        // 1. Repository 함수 호출 횟수 및 파라미터 확인
        verify(exactly = 1) {
            mockRepository.getTourDataToiletInfo(targetContentId)
        }

        // 2. Flow 스트림 그대로 넘어왔는지 확인
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is DataResource.Loading)

        val successEmission = emissions[1] as DataResource.Success
        assertEquals("장애인 주차구역", successEmission.data.parking)
    }
}