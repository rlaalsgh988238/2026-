package com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState

import java.time.LocalDate
import java.time.YearMonth

data class CalendarDayUiModel(
    val date: LocalDate?, // null이면 빈 칸(Spacer)
    val dayNumber: Int,
    val isStart: Boolean = false,
    val isEnd: Boolean = false,
    val isInRange: Boolean = false,
    val isWeekend: Boolean = false,
    val isPast: Boolean = false // 🌟 과거 날짜 여부 추가
)

data class CalendarMonthUiModel(
    val yearMonth: YearMonth,
    val title: String,
    val weeks: List<List<CalendarDayUiModel>>
)

data class DateSelectionState(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val targetMonths: List<YearMonth> = emptyList(),
    val calendarMonths: List<CalendarMonthUiModel> = emptyList()
) {
    val isNextButtonEnabled: Boolean
        get() = startDate != null && endDate != null
}

sealed class DateSelectionEvent {
    data class OnDateSelected(val date: LocalDate) : DateSelectionEvent()
    object OnLoadMoreMonths : DateSelectionEvent()
    object OnNextButtonClicked : DateSelectionEvent()
    object OnBackButtonClicked : DateSelectionEvent()
}

sealed class DateSelectionEffect {
    object NavigateToNextScreen : DateSelectionEffect()
    object NavigateBack : DateSelectionEffect()
}
