package com.tourdataproject.presentation.viewmodel.course.addSchedule.uiState

import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel

data class AddScheduleDetailUiState(
    val isSaving: Boolean = false,
    val placeName: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val memo: String = "",
    // 계산된 결과가 담길 곳 (초기값은 null)
    val accessibilityInfo: AccessibilityInfoUiModel? = null
) {
    val isValid: Boolean
        get() = placeName.isNotBlank() && !isSaving
}

sealed interface AddScheduleDetailEffect {
    data class SubmitSchedule(
        val placeName: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val memo: String,
        val accessibilityInfo: AccessibilityInfoUiModel?
    ) : AddScheduleDetailEffect

    object NavigateBack : AddScheduleDetailEffect
}
