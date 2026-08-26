package com.braveberry.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.braveberry.local.impl.CourseDataSourceImpl
import com.braveberry.local.roomDB.dao.CourseDao
import com.braveberry.toilet_data.course_data.model.AccessibilityInfoDataModel
import com.braveberry.toilet_data.course_data.model.CourseDataModel
import com.braveberry.toilet_data.course_data.model.DayPlanDataModel
import com.braveberry.toilet_data.course_data.model.ScheduleItemDataModel
// ❌ import kotlinx.coroutines.flow.first (이제 필요 없으므로 삭제!)
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braveberry.local.roomDB.AppDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.jvm.java

@RunWith(AndroidJUnit4::class)
class CourseDataSourceImplTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CourseDao
    private lateinit var dataSource: CourseDataSourceImpl

    // 테스트용 더미 데이터
    private val dummyCourse = CourseDataModel(
        courseId = "test_course_1",
        destination = "제주도",
        courseName = "힐링 여행",
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
                        memo = "렌트카 찾기",
                        latitude = 33.5104,
                        longitude = 126.4913,
                        placeId = "place_1",
                        address = "제주국제공항",
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

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = database.courseDao()
        dataSource = CourseDataSourceImpl(dao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun saveCourse_and_getCourseById_works_perfectly() = runBlocking {
        // Given
        dataSource.saveCourse(dummyCourse)

        // 🌟 수정: Flow가 아니므로 .first() 삭제하고 바로 받음!
        val result = dataSource.getCourseById("test_course_1")

        // Then
        assertEquals(dummyCourse.courseName, result?.courseName)

        val savedScheduleName = result?.dayPlans?.get(0)?.schedules?.get(0)?.scheduleName
        assertEquals("제주 공항 도착", savedScheduleName)
    }

    @Test
    fun deleteCourse_works_perfectly() = runBlocking {
        // Given
        dataSource.saveCourse(dummyCourse)

        // 🌟 수정: .first() 삭제!
        val beforeDelete = dataSource.getAllCourses()
        assertEquals(1, beforeDelete.size)

        // When
        dataSource.deleteCourse("test_course_1")

        // 🌟 수정: .first() 삭제!
        val afterDeleteAll = dataSource.getAllCourses()
        val afterDeleteSingle = dataSource.getCourseById("test_course_1")

        // Then
        assertEquals(0, afterDeleteAll.size)
        assertNull(afterDeleteSingle)
    }
}