package com.tourdataproject.presentation.viewmodel.course.makeCourse.uiState

sealed interface CourseIntent {
    object OnBackButtonClicked : CourseIntent
    object OnInfoButtonClicked : CourseIntent
    object OnShareButtonClicked : CourseIntent
    object OnMapButtonClicked : CourseIntent

    data class OnAddScheduleClicked(val dayNumber: Int) : CourseIntent
    data class OnScheduleItemClicked(val scheduleId: String) : CourseIntent
    data class OnEditScheduleButtonClicked(val dayNumber: Int): CourseIntent
    object OnAddStayButtonClicked: CourseIntent

    object OnViewFullMapButtonClicked : CourseIntent
}

sealed interface CourseEffect {
    object NavigateBack : CourseEffect
    object NavigateToCourseInfo : CourseEffect
    object ShareCourse : CourseEffect
    object NavigateToMapScreen : CourseEffect

    data class NavigateToEditSchedule(val dayNumber: Int): CourseEffect
    data class NavigateToAddSchedule(val dayNumber: Int) : CourseEffect
    data class ShowToast(val message: String) : CourseEffect
    object NavigateToAddStay: CourseEffect

    object NavigateToFullMap : CourseEffect
}