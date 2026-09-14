package com.tourdataproject.presentation.viewmodel.course.addSchedule.uiState

import com.tourdataproject.presentation.model.plan.AccessibilityInfoPresentationModel

data class AddScheduleDetailUiState(
    val isSaving: Boolean = false,
    val placeName: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val memo: String = "",
    // 계산된 결과가 담길 곳 (초기값은 null)
    val accessibilityInfo: AccessibilityInfoPresentationModel? = null
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
        val accessibilityInfo: AccessibilityInfoPresentationModel?
    ) : AddScheduleDetailEffect

    object NavigateBack : AddScheduleDetailEffect
}
