package com.braveberry.local

import com.braveberry.local.roomDB.AppDatabase
import kotlin.jvm.java

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braveberry.local.model.course.AccessibilityInfoLocalModel
import com.braveberry.local.model.course.CourseLocalModel
import com.braveberry.local.model.course.DayPlanLocalModel
import com.braveberry.local.model.course.ScheduleItemLocalModel
import com.braveberry.local.roomDB.dao.CourseDao
// ❌ import kotlinx.coroutines.flow.first (삭제!)
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CourseDao

    private val dummyLocalCourse = CourseLocalModel(
        courseId = "db_test_course_1",
        destination = "제주도",
        courseName = "Dao 테스트 코스",
        startDate = 1700000000L,
        endDate = 1700086400L,
        dayPlans = listOf(
            DayPlanLocalModel(
                dayNumber = 1,
                date = 1700000000L,
                schedules = listOf(
                    ScheduleItemLocalModel(
                        scheduleId = "sch_1",
                        order = 1,
                        scheduleName = "공항 도착 및 렌트카",
                        visitTime = "10:00",
                        memo = "차량 사진 찍기",
                        latitude = 33.5104,
                        longitude = 126.4913,
                        placeId = "place_1",
                        address = "제주국제공항",
                        category = "교통",
                        accessibilityInfo = AccessibilityInfoLocalModel(
                            status = "GOOD",
                            safetyScore = 100,
                            planAToiletId = "toilet_1",
                            planBToiletId = null
                        )
                    )
                )
            )
        )
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.courseDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetCourseData() = runBlocking {
        // Given
        dao.insert(dummyLocalCourse)

        // 🌟 수정: Flow가 아니므로 .first() 삭제!
        val loadedData = dao.getCourseById("db_test_course_1")

        // Then
        assertEquals(dummyLocalCourse.courseName, loadedData?.courseName)

        val loadedScheduleName = loadedData?.dayPlans?.get(0)?.schedules?.get(0)?.scheduleName
        assertEquals("공항 도착 및 렌트카", loadedScheduleName)
    }

    @Test
    fun deleteCourseData() = runBlocking {
        // Given
        dao.insert(dummyLocalCourse)

        // 🌟 수정: .first() 삭제!
        val beforeDeleteList = dao.getAllCourses()
        assertEquals(1, beforeDeleteList.size)

        // When
        dao.deleteCourseById("db_test_course_1")

        // 🌟 수정: .first() 삭제!
        val afterDeleteList = dao.getAllCourses()
        val afterDeleteSingle = dao.getCourseById("db_test_course_1")

        // Then
        assertEquals(0, afterDeleteList.size)
        assertNull(afterDeleteSingle)
    }
}