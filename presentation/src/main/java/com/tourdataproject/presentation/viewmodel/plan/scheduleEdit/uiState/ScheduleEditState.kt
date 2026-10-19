package com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState

import com.tourdataproject.presentation.model.plan.ScheduleEditResult
import com.tourdataproject.presentation.model.plan.ScheduleItemPresentationModel

data class ScheduleEditState(
    val dayNumber: Int = 1,
    val dateLabel: String = "",
    val schedules: List<ScheduleItemPresentationModel> = emptyList(),
    val stay: ScheduleItemPresentationModel? = null,
    val isLoading: Boolean = false,
    val deletedStayId: String? = null
){
    val dayLabel: String = "Day $dayNumber"
}

sealed class ScheduleEditIntent {
    data class OnSaveCompleted(val accepted: Boolean) : ScheduleEditIntent()
    data class OnScheduleDeleted(val scheduleId: String) : ScheduleEditIntent()
    data class OnScheduleMoved(val fromIndex: Int, val toIndex: Int) : ScheduleEditIntent()
    object OnScheduleMoveFinished : ScheduleEditIntent()
    object OnSaveClicked : ScheduleEditIntent()
    object OnBackClicked : ScheduleEditIntent()
    object OnStayDeleted : ScheduleEditIntent()
}

sealed class ScheduleEditEffect {
    object NavigateBack : ScheduleEditEffect()
    data class ShowToast(val message: String) : ScheduleEditEffect()
    data class SaveRequested(val result: ScheduleEditResult) : ScheduleEditEffect()
}
