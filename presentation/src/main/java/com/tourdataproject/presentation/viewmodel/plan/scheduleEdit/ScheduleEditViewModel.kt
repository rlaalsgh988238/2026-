package com.tourdataproject.presentation.viewmodel.plan.scheduleEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tourdataproject.presentation.model.plan.DayPlanPresentationModel
import com.tourdataproject.presentation.model.plan.ScheduleEditResult
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEffect
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditIntent
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 한 화면 진입의 편집 초안만 소유합니다. 공유 ViewModel을 알지 못합니다. */
class ScheduleEditViewModel(initialDay: DayPlanPresentationModel) : ViewModel() {
    private val TAG = "ScheduleEditViewModel"
    private val _state = MutableStateFlow(ScheduleEditState(
        dayNumber = initialDay.rawDayNumber,
        dateLabel = initialDay.dateLabel,
        schedules = initialDay.schedules.toList(),
        stay = initialDay.stay.takeIf { it.scheduleId.isNotBlank() }
    ))
    val state: StateFlow<ScheduleEditState> = _state.asStateFlow()

    companion object {
        fun factory(initialDay: DayPlanPresentationModel) = viewModelFactory {
            initializer { ScheduleEditViewModel(initialDay) }
        }
    }

    private val _effect = MutableSharedFlow<ScheduleEditEffect>()
    val effect: SharedFlow<ScheduleEditEffect> = _effect.asSharedFlow()

    fun onIntent(intent: ScheduleEditIntent) {
        // 저장 요청 처리 중에는 초안을 변경하거나 중복 제출하지 않습니다.
        if (_state.value.isLoading && intent !is ScheduleEditIntent.OnSaveCompleted) return
        when (intent) {
            is ScheduleEditIntent.OnSaveCompleted -> completeSave(intent.accepted)
            is ScheduleEditIntent.OnBackClicked -> {
                viewModelScope.launch { _effect.emit(ScheduleEditEffect.NavigateBack) }
            }
            is ScheduleEditIntent.OnSaveClicked -> {
                saveCourse()
            }
            is ScheduleEditIntent.OnScheduleDeleted -> {
                deleteSchedule(intent.scheduleId)
            }
            is ScheduleEditIntent.OnScheduleMoved -> {
                moveSchedule(intent.fromIndex, intent.toIndex)
            }
            is ScheduleEditIntent.OnScheduleMoveFinished -> {
                reorderSchedules()
            }

            is ScheduleEditIntent.OnStayDeleted -> deleteStay()
        }
    }

    private fun deleteStay() {
        val stayId = _state.value.stay?.scheduleId ?: return
        // 공유 상태는 저장할 때만 변경합니다. 반복 삭제도 기존 삭제 ID를 유지합니다.
        _state.update { it.copy(stay = null, deletedStayId = stayId) }
    }

    // 숙소는 이제 schedules 리스트에 섞여있지 않으므로,
    // 마지막 인덱스를 이동 금지시키던 예외 처리를 제거하고 전부 이동 가능하게 처리
    private fun moveSchedule(fromIndex: Int, toIndex: Int) {
        Log.d("ScheduleEditVM", "일정 이동 요청: fromIndex=$fromIndex, toIndex=$toIndex")
        _state.update { currentState ->
            val mutableSchedules = currentState.schedules.toMutableList()

            if (fromIndex in mutableSchedules.indices && toIndex in mutableSchedules.indices) {
                val item = mutableSchedules.removeAt(fromIndex)
                mutableSchedules.add(toIndex, item)
            }
            currentState.copy(schedules = mutableSchedules)
        }
    }

    private fun reorderSchedules() {
        _state.update { currentState ->
            val reordered = currentState.schedules.mapIndexed { index, schedule ->
                schedule.copy(order = index + 1)
            }
            Log.d("ScheduleEditVM", "일정 순서 재정렬 완료")
            currentState.copy(schedules = reordered)
        }
    }

    private fun deleteSchedule(scheduleId: String) {
        _state.update { currentState ->
            val filtered = currentState.schedules.filterNot { it.scheduleId == scheduleId }
            val reordered = filtered.mapIndexed { index, schedule ->
                schedule.copy(order = index + 1)
            }
            currentState.copy(schedules = reordered)
        }
    }

    private fun saveCourse() {
        val draft = _state.value
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            _effect.emit(ScheduleEditEffect.SaveRequested(ScheduleEditResult(
                targetDay = draft.dayNumber,
                schedules = draft.schedules.mapIndexed { index, item -> item.copy(order = index + 1) },
                deletedStayId = draft.deletedStayId
            )))
        }
    }

    private fun completeSave(accepted: Boolean) {
        if (!_state.value.isLoading) return
        _state.update { it.copy(isLoading = false) }
        viewModelScope.launch {
            if (accepted) {
                _effect.emit(ScheduleEditEffect.ShowToast("일정이 저장되었습니다."))
                _effect.emit(ScheduleEditEffect.NavigateBack)
            } else {
                _effect.emit(ScheduleEditEffect.ShowToast("편집할 날짜가 없어 변경 사항을 반영하지 못했습니다."))
            }
        }
    }
}
