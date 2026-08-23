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
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseDataSource: CourseDataSource
) : CourseRepository {

    override suspend fun saveCourse(course: TravelCourse) {
        courseDataSource.saveCourse(course.toDataModel())
    }

    override fun getAllCourses(): Flow<DataResource<List<TravelCourse>>> {
        return courseDataSource.getAllCourses()
            .asDataResourceFlow()
            .mapListDataResource { it.toDomain() }
    }

    override fun getCourseById(courseId: String): Flow<DataResource<TravelCourse?>> {
        return courseDataSource.getCourseById(courseId)
            .asDataResourceFlow()
            .mapDataResource { it?.toDomain() }
    }

    override suspend fun deleteCourse(courseId: String) {
        courseDataSource.deleteCourse(courseId)
    }
}