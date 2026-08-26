package com.braveberry.toilet_data.course_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.toilet_data.course_data.datasource.CourseDataSource
import com.braveberry.toilet_data.course_data.model.*
import com.tourdataproject.domain.model.course.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CourseRepositoryImplTest {

    private lateinit var mockDataSource: CourseDataSource
    private lateinit var repository: CourseRepositoryImpl

    @Before
    fun setup() {
        mockDataSource = mockk(relaxed = true)
        repository = CourseRepositoryImpl(mockDataSource)
    }

    // =====================================================================
    // 테스트용 더미 데이터 세팅 (기존과 동일)
    // =====================================================================

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
                            status = AccessibilityStatus.GOOD,
                            safetyScore = 95,
                            planAToiletId = "toilet_1",
                            planBToiletId = null
                        )
                    )
                )
            )
        )
    )

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
                            status = "GOOD",
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
        repository.saveCourse(dummyDomainCourse)

        coVerify(exactly = 1) {
            mockDataSource.saveCourse(any())
        }
    }

    @Test
    fun `getAllCourses_returns_DataResource_Success_with_mapped_DomainModel`() = runBlocking {
        // 🌟 수정 1: every -> coEvery, flowOf(리스트) -> 그냥 리스트!
        coEvery { mockDataSource.getAllCourses() } returns listOf(dummyDataCourse)

        // 🌟 수정 2: Loading 상태가 없으므로 깔끔하게 first()만 호출!
        val result = repository.getAllCourses().first()

        assertTrue("결과가 Success 상태여야 합니다.", result is DataResource.Success)

        val domainList = (result as DataResource.Success).data
        assertEquals(1, domainList.size)
        assertEquals("test_course_1", domainList[0].courseId)

        val mappedStatus = domainList[0].dayPlans[0].schedules[0].accessibilityInfo.status
        assertEquals(AccessibilityStatus.GOOD, mappedStatus)
    }

    @Test
    fun `getCourseById_returns_DataResource_Success_with_mapped_DomainModel`() = runBlocking {
        // 🌟 수정 3: every -> coEvery, flowOf(데이터) -> 그냥 데이터!
        coEvery { mockDataSource.getCourseById("test_course_1") } returns dummyDataCourse

        // 🌟 수정 4: 깔끔하게 first() 호출
        val result = repository.getCourseById("test_course_1").first()

        assertTrue("결과가 Success 상태여야 합니다.", result is DataResource.Success)

        val domainData = (result as DataResource.Success).data
        assertEquals("test_course_1", domainData?.courseId)
    }

    @Test
    fun `deleteCourse_calls_dataSource_deleteCourse`() = runBlocking {
        repository.deleteCourse("test_course_1")

        coVerify(exactly = 1) {
            mockDataSource.deleteCourse("test_course_1")
        }
    }
}