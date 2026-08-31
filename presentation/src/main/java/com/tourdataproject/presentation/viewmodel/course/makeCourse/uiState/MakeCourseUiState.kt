package com.tourdataproject.presentation.viewmodel.course.uiState

import com.tourdataproject.presentation.model.course.TravelCourseUiModel

data class CourseState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val course: TravelCourseUiModel? = null,
    val errorMessage: String? = null
)

sealed interface CourseEvent {
    object OnBackButtonClicked : CourseEvent
    object OnInfoButtonClicked : CourseEvent
    object OnShareButtonClicked : CourseEvent

    // TODO: 기획변경 유의 하단 버튼 이벤트
    object OnMapButtonClicked : CourseEvent
    object OnNextButtonClicked : CourseEvent
    //저장 버튼
    data class OnSaveButtonClicked(val finalCourse: TravelCourseUiModel) : CourseEvent

    data class OnAddScheduleClicked(val dayNumber: Int) : CourseEvent
    data class OnScheduleItemClicked(val scheduleId: String) : CourseEvent
}


sealed interface CourseEffect {
    object NavigateBack : CourseEffect
    object NavigateToCourseInfo : CourseEffect
    object ShareCourse : CourseEffect          // 공유 버튼 눌렀을 때

    data class NavigateToAddSchedule(val courseId: String, val dayNumber: Int) : CourseEffect

    object NavigateToMapScreen : CourseEffect
    object NavigateToHomeScreen : CourseEffect
    data class ShowToast(val message: String) : CourseEffect
}