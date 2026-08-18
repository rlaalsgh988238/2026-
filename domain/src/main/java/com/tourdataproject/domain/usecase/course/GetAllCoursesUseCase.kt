package com.tourdataproject.domain.usecase.course

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
import com.tourdataproject.domain.model.course.TravelCourse
import com.tourdataproject.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    operator fun invoke() =
        courseRepository.getAllCourses()
            .onSuccess {

            }
            .onError {
                // TODO: 에러
            }
}