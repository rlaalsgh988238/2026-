package com.tourdataproject.domain.usecase.course

import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
import com.tourdataproject.domain.model.course.TravelCourse
import com.tourdataproject.domain.repository.CourseRepository
import javax.inject.Inject

class GetCourseByIdUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
     operator fun invoke(courseId: String) =
        courseRepository.getCourseById(courseId)
            .onSuccess {

            }
            .onError {

            }
}