package com.tourdataproject.impl

import com.braveberry.data_resource.DataResource
import com.tourdataproject.dataSource.TourDataSource
import com.tourdataproject.model.TourDataModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TourDataRepositoryImplTest {

    // 1. mock() 대신 mockk()를 사용합니다.
    private lateinit var mockDataSource: TourDataSource
    private lateinit var repository: TourDataRepositoryImpl

    @Before
    fun setUp() {
        mockDataSource = mockk()
        repository = TourDataRepositoryImpl(mockDataSource)
    }

    @Test
    fun `getContentId 호출 시 DataSource의 Flow를 그대로 반환한다`() = runTest {
        // Given: whenever(...) 대신 every { ... } returns ... 를 사용합니다.
        val expectedId = "987654"
        every {
            mockDataSource.getContentId(any(), any(), any())
        } returns flowOf(DataResource.Success(expectedId))

        // When
        val emissions = repository.getContentId(37.5, 127.0, 1000).toList()

        // Then
        assertEquals(1, emissions.size)
        assertTrue(emissions[0] is DataResource.Success)
        assertEquals(expectedId, (emissions[0] as DataResource.Success).data)
    }

    @Test
    fun `getTourDataToiletInfo 호출 시 Loading 방출 후 DataModel을 DomainModel로 매핑하여 반환한다`() = runTest {
        // Given
        val mockDataModel = TourDataModel(
            contentId = "987654",
            parking = "장애인 주차장 있음",
            route = null,
            elevator = "엘리베이터 있음",
            restroom = "장애인 화장실 있음",
            wheelchair = null,
            exit = null
        )

        // Flow를 리턴하는 일반 함수이므로 every를 사용합니다.
        // (만약 suspend 함수였다면 coEvery를 사용해야 합니다)
        every {
            mockDataSource.getTourDataToiletInfo("987654")
        } returns flowOf(DataResource.Success(mockDataModel))

        // When
        val emissions = repository.getTourDataToiletInfo("987654").toList()

        // Then
        assertEquals(2, emissions.size)

        assertTrue(emissions[0] is DataResource.Loading)

        val successEmission = emissions[1] as DataResource.Success
        val domainModel = successEmission.data

        // (도메인 모델 필드명에 맞게 검증)
        assertEquals("장애인 주차장 있음", domainModel.parking)
        assertEquals("엘리베이터 있음", domainModel.elevator)
    }
}