package com.tourdataproject.presentation.viewmodel.plan.dateSelect

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.viewmodel.base.BaseViewModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.CalendarDayPresentationModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.CalendarMonthPresentationModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionIntent
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DateSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DateSelectionState>(
    savedStateHandle,
    DateSelectionState()
) {

    private val _effect = Channel<DateSelectionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val loadMoreCount = 12

    init {
        val currentMonth = YearMonth.now()
        _state.update {
            it.copy(
                targetMonths = (0..5).map { offset ->
                    currentMonth.plusMonths(offset.toLong())
                }
            )
        }
    }

    fun onIntent(intent: DateSelectionIntent) {
        when (intent) {
            DateSelectionIntent.OnLoadMoreMonths -> {
                loadMoreMonths()
            }

            DateSelectionIntent.OnNextButtonClicked -> {
                viewModelScope.launch {
                    _effect.send(DateSelectionEffect.NavigateToNextScreen)
                }
            }

            DateSelectionIntent.OnBackButtonClicked -> {
                viewModelScope.launch {
                    _effect.send(DateSelectionEffect.NavigateBack)
                }
            }

            is DateSelectionIntent.OnInitializeEdit -> {
                initializeEdit(intent)
            }

            is DateSelectionIntent.OnEditDateTapped -> {
                selectEditDate(intent.date)
            }
        }
    }

    private fun initializeEdit(
        intent: DateSelectionIntent.OnInitializeEdit
    ) {
        if (intent.courseId.isBlank()) return

        val current = _state.value
        if (
            current.isEditInitialized &&
            current.editCourseId == intent.courseId
        ) {
            return
        }

        val start = intent.startMillis
            .takeIf { it != 0L }
            ?.toLocalDate()

        val end = intent.endMillis
            .takeIf { it != 0L }
            ?.toLocalDate()

        val currentMonth = YearMonth.now()
        val startMonth = start?.let { YearMonth.from(it) }

        // 過去の旅行も表示できるよう、開始月を含めます。
        val firstMonth = if (
            startMonth != null &&
            startMonth.isBefore(currentMonth)
        ) {
            startMonth
        } else {
            currentMonth
        }

        val defaultLastMonth = currentMonth.plusMonths(5)
        val endMonth = end?.let { YearMonth.from(it) }

        val lastMonth = if (
            endMonth != null &&
            endMonth.isAfter(defaultLastMonth)
        ) {
            endMonth
        } else {
            defaultLastMonth
        }

        val months = generateSequence(firstMonth) {
            it.plusMonths(1)
        }.takeWhile {
            !it.isAfter(lastMonth)
        }.toList()

        _state.update {
            it.copy(
                editCourseId = intent.courseId,
                isEditInitialized = true,
                editStartMillis = start?.toEpochMillis(),
                editEndMillis = end?.toEpochMillis(),
                targetMonths = months
            )
        }
    }

    private fun selectEditDate(date: LocalDate) {
        _state.update { current ->
            if (!current.isEditInitialized) {
                return@update current
            }

            val start = current.editStartMillis?.toLocalDate()
            val end = current.editEndMillis?.toLocalDate()

            val newRange: Pair<LocalDate, LocalDate?> = when {
                start == null || end != null -> date to null
                date.isBefore(start) -> date to null

                // 시작일을 다시 선택하면 당일 여행
                else -> start to date
            }

            current.copy(
                editStartMillis = newRange.first.toEpochMillis(),
                editEndMillis = newRange.second?.toEpochMillis()
            )
        }
    }

    private fun loadMoreMonths() {
        _state.update { current ->
            val last = current.targetMonths.lastOrNull()
                ?: return@update current

            val more = (1..loadMoreCount).map {
                last.plusMonths(it.toLong())
            }

            current.copy(
                targetMonths = current.targetMonths + more
            )
        }
    }

    fun generateCalendarMonths(
        yearMonths: List<YearMonth>,
        startMillis: Long?,
        endMillis: Long?,
        minSelectableDate: LocalDate? = null,
        maxSelectableDate: LocalDate? = null,
        allowPastDates: Boolean = false
    ): List<CalendarMonthPresentationModel> {
        val today = LocalDate.now()
        val startDate = startMillis?.toLocalDate()
        val endDate = endMillis?.toLocalDate()

        return yearMonths.map { yearMonth ->
            val firstDay = yearMonth.atDay(1)
            val firstDayOffset = firstDay.dayOfWeek.value % 7
            val daysInMonth = yearMonth.lengthOfMonth()
            val rows = (firstDayOffset + daysInMonth + 6) / 7

            val days = (0 until rows * 7).map { cellIndex ->
                val dayNumber = cellIndex - firstDayOffset + 1

                if (dayNumber !in 1..daysInMonth) {
                    CalendarDayPresentationModel(
                        date = null,
                        dayNumber = 0
                    )
                } else {
                    val date = yearMonth.atDay(dayNumber)

                    val isOutOfRange =
                        (minSelectableDate != null &&
                                date.isBefore(minSelectableDate)) ||
                                (maxSelectableDate != null &&
                                        date.isAfter(maxSelectableDate))

                    if (isOutOfRange) {
                        CalendarDayPresentationModel(
                            date = null,
                            dayNumber = 0
                        )
                    } else {
                        CalendarDayPresentationModel(
                            date = date,
                            dayNumber = dayNumber,
                            isStart = date == startDate,
                            isEnd = date == endDate,
                            isInRange = startDate != null &&
                                    endDate != null &&
                                    date.isAfter(startDate) &&
                                    date.isBefore(endDate),
                            isWeekend = cellIndex % 7 == 0 ||
                                    cellIndex % 7 == 6,
                            isPast = !allowPastDates &&
                                    date.isBefore(today)
                        )
                    }
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

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

private fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
