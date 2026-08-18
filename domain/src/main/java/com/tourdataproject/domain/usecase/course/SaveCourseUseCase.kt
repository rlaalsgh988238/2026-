package com.tourdataproject.domain.usecase.course

import com.tourdataproject.domain.model.course.TravelCourse
import com.tourdataproject.domain.repository.CourseRepository
import javax.inject.Inject

class SaveCourseUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(course: TravelCourse) {
        repository.saveCourse(course)
    }
}