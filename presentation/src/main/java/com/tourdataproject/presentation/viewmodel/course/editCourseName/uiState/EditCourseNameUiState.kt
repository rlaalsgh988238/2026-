package com.tourdataproject.presentation.viewmodel.course.editCourseName.uiState

data class EditCourseNameUiState(
    val isLoading: Boolean = false // 저장 중일 때 중복 클릭 방지용
)

sealed interface EditCourseNameIntent {
    data class OnSaveClicked(val courseId: String, val newName: String) : EditCourseNameIntent
    object OnBackClicked : EditCourseNameIntent
}

sealed interface EditCourseNameEffect {
    object NavigateBack : EditCourseNameEffect
    data class ShowToast(val message: String) : EditCourseNameEffect
}