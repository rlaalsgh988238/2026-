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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val courseDataSource: CourseDataSource
) : CourseRepository {

    override suspend fun saveCourse(course: TravelCourse) {
        courseDataSource.saveCourse(course.toDataModel())
    }

    // 🌟 유저님의 mapListDataResource 덕분에 로직이 단 3줄로 끝납니다!
    override fun getAllCourses(): Flow<DataResource<List<TravelCourse>>> {
        return courseDataSource.getAllCourses()              // 1. 날것의 Flow<List<CourseDataModel>>
            .asDataResourceFlow()                            // 2. Flow<DataResource<List<CourseDataModel>>> 로 포장 (로딩, 에러 처리 끝)
            .mapListDataResource { it.toDomain() }           // 3. 내부 리스트 아이템들을 Domain으로 싹 매핑!
    }

    // 🌟 여기도 mapDataResource를 써서 한 방에 해결!
    override fun getCourseById(courseId: String): Flow<DataResource<TravelCourse?>> {
        return courseDataSource.getCourseById(courseId)      // 1. 날것의 Flow<CourseDataModel?>
            .asDataResourceFlow()                            // 2. 포장
            .mapDataResource { it?.toDomain() }              // 3. 단일 아이템 Domain 매핑!
    }

    override suspend fun deleteCourse(courseId: String) {
        courseDataSource.deleteCourse(courseId)
    }
}