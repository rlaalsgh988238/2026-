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

    override suspend fun saveCourse(course: CourseDataModel) {
        courseDao.insert(course.toLocalModel())
    }

    override suspend fun getAllCourses(): List<CourseDataModel> {
        return courseDao.getAllCourses().map { it.toData() }
    }

    override suspend fun getCourseById(courseId: String): CourseDataModel? {
        return courseDao.getCourseById(courseId)?.toData()
    }

    override suspend fun deleteCourse(courseId: String) {
        courseDao.deleteCourseById(courseId)
    }

    override suspend fun updateCourseName(courseId: String, newName: String) {
        courseDao.updateCourseName(courseId, newName)
    }
}