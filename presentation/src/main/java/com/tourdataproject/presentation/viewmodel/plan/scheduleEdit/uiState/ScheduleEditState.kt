package com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState

import com.tourdataproject.presentation.model.course.ScheduleItemUiModel

data class ScheduleEditState(
    val dayNumber: Int = 1,
    val dateLabel: String = "",
    val schedules: List<ScheduleItemUiModel> = emptyList(),
    val isLoading: Boolean = false
){
    val dayLabel: String = "Day $dayNumber"
}

sealed class ScheduleEditEvent {
    data class OnInit(val dateLabel: String, val schedules: List<ScheduleItemUiModel>) : ScheduleEditEvent()
    data class OnScheduleDeleted(val scheduleId: String) : ScheduleEditEvent()
    data class OnScheduleMoved(val fromIndex: Int, val toIndex: Int) : ScheduleEditEvent()
    object OnScheduleMoveFinished : ScheduleEditEvent()
    object OnSaveClicked : ScheduleEditEvent()
    object OnBackClicked : ScheduleEditEvent()
}

sealed class ScheduleEditEffect {
    object NavigateBack : ScheduleEditEffect()
    data class ShowToast(val message: String) : ScheduleEditEffect()
    data class SaveToShared(val dayNumber: Int, val schedules: List<ScheduleItemUiModel>) : ScheduleEditEffect()
}
