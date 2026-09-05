package com.tourdataproject.presentation.viewmodel.plan.dateSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.CalendarDayPresentationModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.CalendarMonthPresentationModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionIntent
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DateSelectionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DateSelectionState())
    val state: StateFlow<DateSelectionState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DateSelectionEffect>()
    val effect: SharedFlow<DateSelectionEffect> = _effect.asSharedFlow()

    private val loadMoreCount = 12
    private val today = LocalDate.now(ZoneId.systemDefault())

    init {
        val currentMonth = YearMonth.now(ZoneId.systemDefault())
        val initialMonths = (0..5).map { currentMonth.plusMonths(it.toLong()) }
        _state.update {
            it.copy(
                targetMonths = initialMonths,
                calendarMonths = generateCalendarMonths(initialMonths, null, null)
            )
        }
    }

    fun onIntent(intent: DateSelectionIntent) {
        when (intent) {
            is DateSelectionIntent.OnDateSelected -> selectByTap(intent.date)
            is DateSelectionIntent.OnLoadMoreMonths -> loadMoreMonths()
            is DateSelectionIntent.OnNextButtonClicked -> {
                viewModelScope.launch { _effect.emit(DateSelectionEffect.NavigateToNextScreen) }
            }
            is DateSelectionIntent.OnBackButtonClicked -> {
                viewModelScope.launch { _effect.emit(DateSelectionEffect.NavigateBack) }
            }
        }
    }

    private fun selectByTap(clickedDate: LocalDate) {
        if (clickedDate.isBefore(today)) return

        _state.update { current ->
            val start = current.startDate
            val end = current.endDate

            val (newStart, newEnd) = when {
                start == null || (start != null && end != null) -> clickedDate to null
                clickedDate.isBefore(start) -> clickedDate to null
                clickedDate == start -> null to null
                else -> start to clickedDate
            }

            current.copy(
                startDate = newStart,
                endDate = newEnd,
                calendarMonths = generateCalendarMonths(current.targetMonths, newStart, newEnd)
            )
        }
    }

    private fun loadMoreMonths() {
        _state.update { current ->
            val last = current.targetMonths.lastOrNull() ?: return@update current
            val more = (1..loadMoreCount).map { last.plusMonths(it.toLong()) }
            val newMonths = current.targetMonths + more

            current.copy(
                targetMonths = newMonths,
                calendarMonths = generateCalendarMonths(newMonths, current.startDate, current.endDate)
            )
        }
    }

    private fun generateCalendarMonths(
        yearMonths: List<YearMonth>,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): List<CalendarMonthPresentationModel> {
        return yearMonths.map { yearMonth ->
            val firstDayOfMonth = yearMonth.atDay(1)
            val firstDayOffset = if (firstDayOfMonth.dayOfWeek.value == 7) 0 else firstDayOfMonth.dayOfWeek.value
            val daysInMonth = yearMonth.lengthOfMonth()
            val totalCells = firstDayOffset + daysInMonth
            val rows = (totalCells + 6) / 7
            val totalDaysToRender = rows * 7

            val days = (0 until totalDaysToRender).map { cellIndex ->
                val dayNumber = cellIndex - firstDayOffset + 1
                if (dayNumber in 1..daysInMonth) {
                    val currentDate = yearMonth.atDay(dayNumber)
                    val isStart = currentDate == startDate
                    val isEnd = currentDate == endDate
                    val isInRange = startDate != null && endDate != null &&
                            currentDate.isAfter(startDate) && currentDate.isBefore(endDate)
                    val isWeekend = cellIndex % 7 == 0 || cellIndex % 7 == 6
                    val isPast = currentDate.isBefore(today) // 🌟 오늘 이전인지 확인

                    CalendarDayPresentationModel(
                        date = currentDate,
                        dayNumber = dayNumber,
                        isStart = isStart,
                        isEnd = isEnd,
                        isInRange = isInRange,
                        isWeekend = isWeekend,
                        isPast = isPast
                    )
                } else {
                    CalendarDayPresentationModel(date = null, dayNumber = 0)
                }
            }

            CalendarMonthPresentationModel(
                yearMonth = yearMonth,
                title = "${yearMonth.year}년 ${yearMonth.monthValue}월",
                weeks = days.chunked(7)
            )
        }
    }
}
