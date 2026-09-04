package com.tourdataproject.presentation.viewmodel.plan.scheduleEdit

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEffect
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEvent
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val TAG = "ScheduleEditViewModel"

    private val _state = MutableStateFlow(ScheduleEditState())
    val state: StateFlow<ScheduleEditState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ScheduleEditEffect>()
    val effect: SharedFlow<ScheduleEditEffect> = _effect.asSharedFlow()

    val dayNum: Int = checkNotNull(savedStateHandle["dayNum"])

    init {
        Log.d(TAG, "편집 중인 날짜 번호: $dayNum")
        setDayNum(dayNum)
    }

    fun setEvent(event: ScheduleEditEvent) {
        when (event) {
            is ScheduleEditEvent.OnInit -> {
                _state.update { it.copy(dateLabel = event.dateLabel, schedules = event.schedules) }
            }
            is ScheduleEditEvent.OnBackClicked -> {
                viewModelScope.launch { _effect.emit(ScheduleEditEffect.NavigateBack) }
            }
            is ScheduleEditEvent.OnSaveClicked -> {
                saveCourse()
            }
            is ScheduleEditEvent.OnScheduleDeleted -> {
                deleteSchedule(event.scheduleId)
            }
            is ScheduleEditEvent.OnScheduleMoved -> {
                moveSchedule(event.fromIndex, event.toIndex)
            }
            is ScheduleEditEvent.OnScheduleMoveFinished -> {
                reorderSchedules()
            }
        }
    }

    private fun setDayNum(dayNum: Int){
        _state.update { currentState ->
            currentState.copy(dayNumber = dayNum)
        }
    }

    private fun moveSchedule(fromIndex: Int, toIndex: Int) {
        Log.d("ScheduleEditVM", "일정 이동 요청: fromIndex=$fromIndex, toIndex=$toIndex")
        _state.update { currentState ->
            val mutableSchedules = currentState.schedules.toMutableList()

            if (fromIndex == mutableSchedules.lastIndex || toIndex == mutableSchedules.lastIndex) {
                return@update currentState
            }

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
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // UI에 공유 뷰모델 업데이트를 위임하는 Effect 발생
            _effect.emit(ScheduleEditEffect.SaveToShared(_state.value.dayNumber, _state.value.schedules))

            _state.update { it.copy(isLoading = false) }
            _effect.emit(ScheduleEditEffect.ShowToast("일정이 저장되었습니다."))
            _effect.emit(ScheduleEditEffect.NavigateBack)
        }
    }
}
