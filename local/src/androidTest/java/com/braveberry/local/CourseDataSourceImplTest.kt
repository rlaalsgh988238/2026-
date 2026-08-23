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
import kotlinx.coroutines.flow.first
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
        // 1. 가짜(In-Memory) DB 생성: 테스트 끝나면 램에서 싹 지워짐!
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // 테스트 환경이므로 메인 스레드 동작 허용
            .build()

        // 2. Dao와 DataSource 세팅
        dao = database.courseDao()
        dataSource = CourseDataSourceImpl(dao)
    }

    @After
    fun teardown() {
        // 테스트가 하나 끝날 때마다 DB 문 닫기
        database.close()
    }

    @Test
    fun saveCourse_and_getCourseById_works_perfectly() = runBlocking {
        // Given (준비): 더미 코스 데이터를 DB에 저장한다.
        dataSource.saveCourse(dummyCourse)

        // When (실행): 저장한 코스를 ID로 다시 불러온다.
        // Flow에서 첫 번째 방출된 데이터를 가져오기 위해 .first() 사용
        val result = dataSource.getCourseById("test_course_1").first()

        // Then (검증): 저장한 데이터와 불러온 데이터가 완벽히 일치하는지 확인한다.
        assertEquals(dummyCourse.courseName, result?.courseName)

        // TypeConverter가 하위 리스트(ScheduleItem)까지 잘 변환했는지 핵심 검증!
        val savedScheduleName = result?.dayPlans?.get(0)?.schedules?.get(0)?.scheduleName
        assertEquals("제주 공항 도착", savedScheduleName)
    }

    @Test
    fun deleteCourse_works_perfectly() = runBlocking {
        // Given (준비): 더미 코스를 저장한다.
        dataSource.saveCourse(dummyCourse)

        // 코스가 잘 들어갔는지 1차 확인
        val beforeDelete = dataSource.getAllCourses().first()
        assertEquals(1, beforeDelete.size)

        // When (실행): 코스를 삭제한다.
        dataSource.deleteCourse("test_course_1")

        // Then (검증): 삭제 후 코스 리스트가 비어있고, 특정 ID로 조회해도 null이 나와야 한다.
        val afterDeleteAll = dataSource.getAllCourses().first()
        val afterDeleteSingle = dataSource.getCourseById("test_course_1").first()

        assertEquals(0, afterDeleteAll.size) // 리스트가 비었는가?
        assertNull(afterDeleteSingle)        // 상세 조회 시 null인가?
    }
}