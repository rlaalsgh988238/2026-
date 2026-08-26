package com.tourdataproject.presentation.viewmodel.plan.uiState

import java.time.LocalDate
import java.time.YearMonth

data class DateSelectionState(
    val selectedDate: LocalDate? = null,
    val targetMonths: List<YearMonth> = listOf(
        YearMonth.now(),
        YearMonth.now().plusMonths(1)
    )
) {
    val isNextButtonEnabled: Boolean
        get() = selectedDate != null
}

sealed interface DateSelectionEvent {
    data class OnDateSelected(val date: LocalDate) : DateSelectionEvent
    object OnNextButtonClicked : DateSelectionEvent
    object OnBackButtonClicked : DateSelectionEvent
}

sealed interface DateSelectionEffect {
    object NavigateToNextScreen : DateSelectionEffect
    object NavigateBack : DateSelectionEffect
}