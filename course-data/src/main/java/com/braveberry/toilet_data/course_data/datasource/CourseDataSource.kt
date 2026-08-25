package com.braveberry.toilet_data.course_data.datasource

import com.braveberry.toilet_data.course_data.model.CourseDataModel
import kotlinx.coroutines.flow.Flow

interface CourseDataSource {
    suspend fun saveCourse(course: CourseDataModel)
    suspend fun getAllCourses(): List<CourseDataModel>
    suspend fun getCourseById(courseId: String): CourseDataModel?
    suspend fun deleteCourse(courseId: String)
}