package com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState

import java.time.LocalDate
import java.time.YearMonth

data class CalendarDayPresentationModel(
    val date: LocalDate?, // null이면 빈 칸(Spacer)
    val dayNumber: Int,
    val isStart: Boolean = false,
    val isEnd: Boolean = false,
    val isInRange: Boolean = false,
    val isWeekend: Boolean = false,
    val isPast: Boolean = false // 🌟 과거 날짜 여부 추가
)

data class CalendarMonthPresentationModel(
    val yearMonth: YearMonth,
    val title: String,
    val weeks: List<List<CalendarDayPresentationModel>>
)

data class DateSelectionState(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val targetMonths: List<YearMonth> = emptyList(),
    val calendarMonths: List<CalendarMonthPresentationModel> = emptyList()
) {
    val isNextButtonEnabled: Boolean
        get() = startDate != null && endDate != null
}

sealed class DateSelectionIntent {
    data class OnDateSelected(val date: LocalDate) : DateSelectionIntent()
    object OnLoadMoreMonths : DateSelectionIntent()
    object OnNextButtonClicked : DateSelectionIntent()
    object OnBackButtonClicked : DateSelectionIntent()
}

sealed class DateSelectionEffect {
    object NavigateToNextScreen : DateSelectionEffect()
    object NavigateBack : DateSelectionEffect()
}
