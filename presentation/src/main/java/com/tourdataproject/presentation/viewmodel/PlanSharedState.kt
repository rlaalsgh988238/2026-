package com.tourdataproject.presentation.viewmodel

import com.tourdataproject.presentation.model.KakaoMapUiModel
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.model.course.TravelCoursePresentationModel
import java.time.LocalDate

data class PlanSharedState(
    val course: TravelCoursePresentationModel = TravelCoursePresentationModel(),
    val currentAddingDayNumber: Int = 1,
    val draftSchedule: ScheduleItemUiModel? = null,
    val draftStartDate: Long? = null,
    val draftEndDate: Long? = null
)

sealed interface PlanSharedIntent {
    data class OnCourseNameChanged(val newName: String) : PlanSharedIntent
    data class OnAddScheduleToDay(val targetDay: Int, val newPlace: ScheduleItemUiModel) : PlanSharedIntent
    data class OnDeleteSchedule(val targetDay: Int, val scheduleIdToRemove: String) : PlanSharedIntent
    data class OnReorderSchedules(val targetDay: Int, val reorderedSchedules: List<ScheduleItemUiModel>) : PlanSharedIntent
    data class OnSetAddingDayNumber(val dayNumber: Int) : PlanSharedIntent
    data class OnSetDraftSchedule(val place: KakaoMapUiModel) : PlanSharedIntent
    data class OnConfirmAndAddSchedule(val memoInput: String, val accessibilityInfo: AccessibilityInfoUiModel?) : PlanSharedIntent
    data class OnCitySelected(val cityName: String) : PlanSharedIntent
    data class OnGetCityPosition(val cityName: String): PlanSharedIntent
    object OnCityDeselected : PlanSharedIntent
    data class OnLoadCourseById(val courseId: String) : PlanSharedIntent
    object OnClearDraftSchedule : PlanSharedIntent
    object ClearPlanState : PlanSharedIntent
    data class OnCalendarDateTapped(val date: LocalDate) : PlanSharedIntent
    object OnConfirmDateSelection : PlanSharedIntent
    object OnSaveCourse : PlanSharedIntent
    data class OnStoreBackUp(val state: PlanSharedState): PlanSharedIntent
    object OnClearBackUp: PlanSharedIntent
}

sealed interface PlanSharedEffect {
    object NavigateToHomeScreen : PlanSharedEffect
    data class ShowToast(val message: String) : PlanSharedEffect
}