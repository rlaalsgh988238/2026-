package com.tourdataproject.presentation.viewmodel.plan

import com.tourdataproject.domain.model.PlanBackup
import com.tourdataproject.presentation.model.KakaoMapPresentationModel
import com.tourdataproject.presentation.model.plan.AccessibilityInfoPresentationModel
import com.tourdataproject.presentation.model.plan.ScheduleItemPresentationModel
import com.tourdataproject.presentation.model.plan.TravelCoursePresentationModel
import java.time.LocalDate

data class PlanSharedState(
    val course: TravelCoursePresentationModel = TravelCoursePresentationModel(),
    val currentAddingDayNumber: Int = 1,
    val draftSchedule: ScheduleItemPresentationModel? = null,
    val draftStartDate: Long? = null,
    val draftEndDate: Long? = null
)

fun PlanSharedState.toBackUp(): PlanBackup =
    PlanBackup(
        course = course.toDomain(),
        currentAddingDayNumber = currentAddingDayNumber,
        draftEndDate = draftEndDate,
        draftStartDate = draftStartDate,
        draftScheduleItem = draftSchedule?.toDomain()
    )

sealed interface PlanSharedIntent {
    data class OnCourseNameChanged(val newName: String) : PlanSharedIntent
    data class OnAddScheduleToDay(val targetDay: Int, val newPlace: ScheduleItemPresentationModel) : PlanSharedIntent
    data class OnDeleteSchedule(val targetDay: Int, val scheduleIdToRemove: String) : PlanSharedIntent
    data class OnReorderSchedules(val targetDay: Int, val reorderedSchedules: List<ScheduleItemPresentationModel>) : PlanSharedIntent
    data class OnSetAddingDayNumber(val dayNumber: Int) : PlanSharedIntent
    data class OnSetDraftSchedule(val place: KakaoMapPresentationModel) : PlanSharedIntent
    data class OnConfirmAndAddSchedule(val memoInput: String, val accessibilityInfo: AccessibilityInfoPresentationModel?) : PlanSharedIntent
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