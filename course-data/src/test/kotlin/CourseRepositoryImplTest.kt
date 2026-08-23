package com.braveberry.toilet_data.course_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.toilet_data.course_data.datasource.CourseDataSource
import com.braveberry.toilet_data.course_data.model.*
import com.tourdataproject.domain.model.course.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CourseRepositoryImplTest {

    // 진짜 DB 대신 사용할 '가짜(Mock)' DataSource
    private lateinit var mockDataSource: CourseDataSource

    // 테스트할 실제 타겟
    private lateinit var repository: CourseRepositoryImpl

    @Before
    fun setup() {
        // MockK를 이용해 가짜 DataSource 객체를 생성합니다.
        mockDataSource = mockk(relaxed = true)
        repository = CourseRepositoryImpl(mockDataSource)
    }

    // =====================================================================
    // 테스트용 더미 데이터 세팅
    // =====================================================================

    // 1. 도메인 레이어 더미 데이터 (TravelCourse) - Enum 사용
    private val dummyDomainCourse = TravelCourse(
        courseId = "test_course_1",
        destination = "제주도",
        courseName = "제주도 힐링 코스",
        startDate = 1700000000L,
        endDate = 1700086400L,
        dayPlans = listOf(
            DayPlan(
                dayNumber = 1,
                date = 1700000000L,
                schedules = listOf(
                    ScheduleItem(
                        scheduleId = "sch_1",
                        order = 1,
                        scheduleName = "제주 공항 도착",
                        visitTime = "10:00",
                        memo = "렌트카 픽업하기",
                        latitude = 33.5104,
                        longitude = 126.4913,
                        placeId = "place_1",
                        address = "제주특별자치도 제주시 공항로 2",
                        category = "교통",
                        accessibilityInfo = AccessibilityInfo(
                            status = AccessibilityStatus.GOOD, // 🌟 Domain 모델은 Enum!
                            safetyScore = 95,
                            planAToiletId = "toilet_1",
                            planBToiletId = null
                        )
                    )
                )
            )
        )
    )

    // 2. 데이터 레이어 더미 데이터 (CourseDataModel) - String 사용
    private val dummyDataCourse = CourseDataModel(
        courseId = "test_course_1",
        destination = "제주도",
        courseName = "제주도 힐링 코스",
        startDate = 1700000000L,
        endDate = 1700086400L,
        dayPlans = listOf(
            DayPlanDataModel(
                dayNumber = 1,
                date = 1700000000L,
                schedules = listOf(
                    ScheduleItemDataModel(
                        scheduleId = "sch_1",
                        order = 1,
                        scheduleName = "제주 공항 도착",
                        visitTime = "10:00",
                        memo = "렌트카 픽업하기",
                        latitude = 33.5104,
                        longitude = 126.4913,
                        placeId = "place_1",
                        address = "제주특별자치도 제주시 공항로 2",
                        category = "교통",
                        accessibilityInfo = AccessibilityInfoDataModel(
                            status = "GOOD", // 🌟 Data 모델은 String!
                            safetyScore = 95,
                            planAToiletId = "toilet_1",
                            planBToiletId = null
                        )
                    )
                )
            )
        )
    )

    // =====================================================================
    // 테스트 케이스 시작
    // =====================================================================

    @Test
    fun `saveCourse_calls_dataSource_saveCourse_with_mapped_DataModel`() = runBlocking {
        // When: Repository의 saveCourse를 호출한다. (Domain -> Data 매핑 발생)
        repository.saveCourse(dummyDomainCourse)

        // Then: 가짜 DataSource의 saveCourse가 1번 호출되었는지 검증한다.
        coVerify(exactly = 1) {
            mockDataSource.saveCourse(any())
        }
    }

    @Test
    fun `getAllCourses_returns_DataResource_Success_with_mapped_DomainModel`() = runBlocking {
        // Given: 가짜 DataSource가 전체 목록을 방출하도록 조작
        every { mockDataSource.getAllCourses() } returns flowOf(listOf(dummyDataCourse))

        // When: Loading 상태를 건너뛰고 첫 번째 진짜 데이터를 잡는다!
        val result = repository.getAllCourses().first { it !is DataResource.Loading }



        // Then:
        assertTrue("결과가 Success 상태여야 합니다.", result is DataResource.Success)

        val domainList = (result as DataResource.Success).data
        assertEquals(1, domainList.size)
        assertEquals("test_course_1", domainList[0].courseId)

        // 🌟 안쪽의 매핑이 잘 되었는지(Enum 변환 등) 검증!
        val mappedStatus = domainList[0].dayPlans[0].schedules[0].accessibilityInfo.status
        assertEquals(AccessibilityStatus.GOOD, mappedStatus)
    }

    @Test
    fun `getCourseById_returns_DataResource_Success_with_mapped_DomainModel`() = runBlocking {
        // Given: 특정 ID로 조회하면 dummyDataCourse 1개를 방출하도록 조작
        every { mockDataSource.getCourseById("test_course_1") } returns flowOf(dummyDataCourse)

        // When: Loading 상태를 건너뛰고 첫 번째 진짜 데이터를 잡는다!
        val result = repository.getCourseById("test_course_1").first { it !is DataResource.Loading }



        // Then
        assertTrue("결과가 Success 상태여야 합니다.", result is DataResource.Success)

        val domainData = (result as DataResource.Success).data
        assertEquals("test_course_1", domainData?.courseId)
    }

    @Test
    fun `deleteCourse_calls_dataSource_deleteCourse`() = runBlocking {
        // When: Repository에서 삭제 명령을 내린다.
        repository.deleteCourse("test_course_1")

        // Then: 가짜 DataSource의 deleteCourse가 해당 ID와 함께 1번 호출되었는지 검증한다.
        coVerify(exactly = 1) {
            mockDataSource.deleteCourse("test_course_1")
        }
    }
}