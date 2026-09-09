package com.tourdataproject.domain.usecase.calculateAceesibility.usecase_test

import com.tourdataproject.domain.usecase.tourData.GetContentIdUseCase


import com.braveberry.data_resource.DataResource
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

class GetContentIdUseCaseTest {

    private lateinit var mockRepository: TourRepository
    private lateinit var useCase: GetContentIdUseCase

    @Before
    fun setUp() {
        mockRepository = mockk()
        useCase = GetContentIdUseCase(mockRepository)
    }

    @Test
    fun `invoke 실행 시 Repository를 정확한 파라미터로 호출하고 Flow를 반환한다`() = runTest {
        // Given
        val targetLat = 37.5
        val targetLng = 127.0
        val targetRadius = 50
        val expectedId = "987654"

        // Repository가 반환할 가짜 Flow 셋업
        every {
            mockRepository.getContentId(targetLat, targetLng, targetRadius)
        } returns flowOf(DataResource.Success(expectedId))

        // When (invoke 연산자 호출)
        val emissions = useCase(targetLat, targetLng, targetRadius).toList()

        // Then
        // 1. Repository의 함수가 정확한 파라미터로 딱 1번 호출되었는지 검증
        verify(exactly = 1) {
            mockRepository.getContentId(targetLat, targetLng, targetRadius)
        }

        // 2. 결과값이 잘 넘어왔는지 검증
        assertEquals(1, emissions.size)
        assertTrue(emissions[0] is DataResource.Success)
        assertEquals(expectedId, (emissions[0] as DataResource.Success).data)
    }
}