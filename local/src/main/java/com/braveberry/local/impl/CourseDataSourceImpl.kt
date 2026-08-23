package com.braveberry.local.impl

import com.braveberry.local.mapper.toData
import com.braveberry.local.model.course.toLocalModel
import com.braveberry.local.roomDB.dao.CourseDao
import com.braveberry.toilet_data.course_data.datasource.CourseDataSource
import com.braveberry.toilet_data.course_data.model.CourseDataModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CourseDataSourceImpl @Inject internal constructor(
    private val courseDao: CourseDao
) : CourseDataSource {

    // 1. 코스 저장 및 수정
    override suspend fun saveCourse(course: CourseDataModel) {
        courseDao.insert(course.toLocalModel())
    }

    // 2. 전체 코스 목록 가져오기
    override fun getAllCourses(): Flow<List<CourseDataModel>> {
        return courseDao.getAllCourses().map { it.toData() }
    }

    // 3. 특정 코스 상세 정보 가져오기
    override fun getCourseById(courseId: String): Flow<CourseDataModel?> {
        return courseDao.getCourseById(courseId).map { it?.toData() }
    }

    // 4. 코스 삭제
    override suspend fun deleteCourse(courseId: String) {
        courseDao.deleteCourseById(courseId)
    }
}