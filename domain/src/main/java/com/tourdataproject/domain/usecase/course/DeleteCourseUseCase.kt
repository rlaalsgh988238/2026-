package com.tourdataproject.domain.usecase.course

import com.tourdataproject.domain.repository.CourseRepository
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DeleteCourseUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(courseId: String) {
        repository.deleteCourse(courseId)

        val flow = flowOf<Int>()

    }
}