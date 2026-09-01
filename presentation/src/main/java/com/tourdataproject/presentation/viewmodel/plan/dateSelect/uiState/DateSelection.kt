package com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState

import java.time.LocalDate
import java.time.YearMonth

data class DateSelectionState(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    // 무한 스크롤을 위해 ViewModel이 관리하는 달 목록
    val targetMonths: List<YearMonth> = (0..11).map { YearMonth.now().plusMonths(it.toLong()) }
) {
    val isNextButtonEnabled: Boolean
        get() = startDate != null && endDate != null
}

sealed interface DateSelectionEvent {
    // 탭으로 날짜 선택
    data class OnDateSelected(val date: LocalDate) : DateSelectionEvent
    // 무한 스크롤: 다음 달들 추가 로드
    object OnLoadMoreMonths : DateSelectionEvent
    object OnNextButtonClicked : DateSelectionEvent
    object OnBackButtonClicked : DateSelectionEvent
}

sealed interface DateSelectionEffect {
    object NavigateToNextScreen : DateSelectionEffect
    object NavigateBack : DateSelectionEffect
}