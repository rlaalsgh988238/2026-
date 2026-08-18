package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.course.TravelCourse
import kotlinx.coroutines.flow.Flow

interface CourseRepository {

    // 1. 코스 저장 및 수정 (Create & Update)
    suspend fun saveCourse(course: TravelCourse)

    // 2. 전체 코스 목록 가져오기
    fun getAllCourses(): Flow<DataResource<List<TravelCourse>>>

    // 3. 특정 코스 상세 정보 가져오기
    fun getCourseById(courseId: String): Flow<DataResource<TravelCourse?>>

    // 4. 코스 삭제 (Delete)
    suspend fun deleteCourse(courseId: String)
}