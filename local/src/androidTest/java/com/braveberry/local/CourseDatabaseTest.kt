package com.braveberry.local

import com.braveberry.local.roomDB.ToiletDatabase
import kotlin.jvm.java

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.braveberry.local.model.*
import com.braveberry.local.roomDB.dao.CourseDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseDatabaseTest {

    private lateinit var db: ToiletDatabase
    private lateinit var dao: CourseDao

    // 테스트용 'Local' 더미 데이터 (DataSource 테스트 때와 다르게 LocalModel을 씁니다!)
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

        // 1. 메모리 DB 생성 (AppDatabase 사용)
        db = Room.inMemoryDatabaseBuilder(
            context, ToiletDatabase::class.java
        )
            .allowMainThreadQueries() // 테스트 환경에서 Flow 처리를 위해 메인 스레드 쿼리 허용
            .build()

        // 2. CourseDao 뽑아오기
        dao = db.courseDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetCourseData() = runBlocking {
        // Given (준비): 코스 데이터를 insert 한다 (BaseDao 기능)
        dao.insert(dummyLocalCourse)

        // When (실행): Flow로 던져주는 단건 데이터를 .first()로 받아온다
        val loadedData = dao.getCourseById("db_test_course_1").first()

        // Then (검증)
        // 1. 최상단 코스 정보가 잘 저장되었는가?
        assertEquals(dummyLocalCourse.courseName, loadedData?.courseName)

        // 2. 🌟 TypeConverter가 중첩 리스트 압축을 완벽하게 해냈는가? (가장 중요)
        val loadedScheduleName = loadedData?.dayPlans?.get(0)?.schedules?.get(0)?.scheduleName
        assertEquals("공항 도착 및 렌트카", loadedScheduleName)
    }

    @Test
    fun deleteCourseData() = runBlocking {
        // Given (준비): 데이터를 넣고 잘 들어갔는지 확인한다
        dao.insert(dummyLocalCourse)
        val beforeDeleteList = dao.getAllCourses().first()
        assertEquals(1, beforeDeleteList.size)

        // When (실행): 커스텀 쿼리로 만든 ID 기반 삭제 함수 호출
        dao.deleteCourseById("db_test_course_1")

        // Then (검증): 지운 후에는 리스트가 비어있어야 하고, 단건 조회 시 null이어야 한다
        val afterDeleteList = dao.getAllCourses().first()
        val afterDeleteSingle = dao.getCourseById("db_test_course_1").first()

        assertEquals(0, afterDeleteList.size)
        assertNull(afterDeleteSingle)
    }
}