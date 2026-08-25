package com.braveberry.toilet_data.course_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.asDataResourceFlow
import com.braveberry.data_resource.mapDataResource
import com.braveberry.data_resource.mapListDataResource
import com.braveberry.toilet_data.course_data.datasource.CourseDataSource
import com.braveberry.toilet_data.course_data.mapper.toDataModel
import com.braveberry.toilet_data.course_data.mapper.toDomain
import com.tourdataproject.domain.model.course.TravelCourse
import com.tourdataproject.domain.repository.CourseRepository
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseDataSource: CourseDataSource
) : CourseRepository {

    override suspend fun saveCourse(course: TravelCourse) {
        courseDataSource.saveCourse(course.toDataModel())
    }

    override fun getAllCourses(): Flow<DataResource<List<TravelCourse>>> = flow<DataResource<List<TravelCourse>>> {
        val resultList = courseDataSource.getAllCourses().map { it.toDomain() }
        emit(DataResource.Success(resultList))
    }.catch { e ->
        emit(DataResource.Error(e))
    }

    override fun getCourseById(courseId: String): Flow<DataResource<TravelCourse?>> = flow<DataResource<TravelCourse?>> {
        val result = courseDataSource.getCourseById(courseId)?.toDomain()
        emit(DataResource.Success(result))
    }.catch { e ->
        emit(DataResource.Error(e))
    }

    override suspend fun deleteCourse(courseId: String) {
        courseDataSource.deleteCourse(courseId)
    }
}