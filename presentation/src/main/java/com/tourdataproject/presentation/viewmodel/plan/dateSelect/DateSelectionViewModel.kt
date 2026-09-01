package com.tourdataproject.presentation.viewmodel.plan.dateSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionEvent
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
import javax.inject.Inject

@HiltViewModel
class DateSelectionViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(DateSelectionState())
    val state: StateFlow<DateSelectionState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DateSelectionEffect>()
    val effect: SharedFlow<DateSelectionEffect> = _effect.asSharedFlow()

    // 한 번에 추가로 불러올 달 수
    private val loadMoreCount = 12

    fun setEvent(event: DateSelectionEvent) {
        when (event) {
            is DateSelectionEvent.OnDateSelected -> selectByTap(event.date)
            is DateSelectionEvent.OnLoadMoreMonths -> loadMoreMonths()
            is DateSelectionEvent.OnNextButtonClicked -> {
                viewModelScope.launch { _effect.emit(DateSelectionEffect.NavigateToNextScreen) }
            }
            is DateSelectionEvent.OnBackButtonClicked -> {
                viewModelScope.launch { _effect.emit(DateSelectionEffect.NavigateBack) }
            }
        }
    }

    // 탭으로 시작/종료 선택
    private fun selectByTap(clickedDate: LocalDate) {
        _state.update { current ->
            val start = current.startDate
            val end = current.endDate
            when {
                // 처음 선택하거나 이미 범위가 완성된 상태 -> 새 시작일
                start == null || (start != null && end != null) ->
                    current.copy(startDate = clickedDate, endDate = null)

                // 시작일보다 이전 날짜 -> 시작일 변경
                clickedDate.isBefore(start) ->
                    current.copy(startDate = clickedDate, endDate = null)

                // 시작일을 다시 탭 -> 선택 취소
                clickedDate == start ->
                    current.copy(startDate = null, endDate = null)

                // 시작일 이후 날짜 -> 종료일 지정
                else ->
                    current.copy(startDate = start, endDate = clickedDate)
            }
        }
    }

    // 무한 스크롤: 마지막 달 뒤로 이어붙이기
    private fun loadMoreMonths() {
        _state.update { current ->
            val last = current.targetMonths.lastOrNull() ?: return@update current
            val more = (1..loadMoreCount).map { last.plusMonths(it.toLong()) }
            current.copy(targetMonths = current.targetMonths + more)
        }
    }
}