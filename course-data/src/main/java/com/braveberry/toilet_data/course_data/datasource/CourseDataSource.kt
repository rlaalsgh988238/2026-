package com.braveberry.toilet_data.course_data.datasource

import com.braveberry.toilet_data.course_data.model.CourseDataModel
import kotlinx.coroutines.flow.Flow

interface CourseDataSource {
    // 1. 코스 저장 및 수정 (1회성 쓰기 -> suspend)
    suspend fun saveCourse(course: CourseDataModel)

    // 2. 전체 코스 목록 가져오기 (실시간 관찰 -> 일반 fun + Flow)
    fun getAllCourses(): Flow<List<CourseDataModel>>

    // 3. 특정 코스 상세 정보 가져오기 (실시간 관찰 -> 일반 fun + Flow)
    fun getCourseById(courseId: String): Flow<CourseDataModel?>

    // 4. 코스 삭제 (1회성 쓰기 -> suspend)
    suspend fun deleteCourse(courseId: String)
}