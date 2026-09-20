package com.tourdataproject.domain.usecase.course

import com.tourdataproject.domain.repository.CourseRepository
import javax.inject.Inject

class UpdateCourseNameUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(courseId: String, newName: String) {
        repository.updateCourseName(courseId, newName)
    }
}