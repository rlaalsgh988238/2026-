// 파일: DateSelectionUiState.kt
package com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState

import com.tourdataproject.presentation.viewmodel.base.BaseState
import java.time.LocalDate
import java.time.YearMonth

data class CalendarDayPresentationModel(
    val date: LocalDate?,
    val dayNumber: Int,
    val isStart: Boolean = false,
    val isEnd: Boolean = false,
    val isInRange: Boolean = false,
    val isWeekend: Boolean = false,
    val isPast: Boolean = false
)

data class CalendarMonthPresentationModel(
    val yearMonth: YearMonth,
    val title: String,
    val weeks: List<List<CalendarDayPresentationModel>>
)

data class DateSelectionState(
    val targetMonths: List<YearMonth> = emptyList(),
    override val entryPoint: String? = null,
    override val purpose: String? = null
) : BaseState<DateSelectionState> {

    override fun setEntryPoint(entryPoint: String?): DateSelectionState =
        copy(entryPoint = entryPoint)

    override fun setPurpose(purpose: String?): DateSelectionState =
        copy(purpose = purpose)
}

sealed interface DateSelectionIntent {
    object OnLoadMoreMonths : DateSelectionIntent
    object OnNextButtonClicked : DateSelectionIntent
    object OnBackButtonClicked : DateSelectionIntent
}

sealed interface DateSelectionEffect {
    object NavigateToNextScreen : DateSelectionEffect
    object NavigateBack : DateSelectionEffect
}
