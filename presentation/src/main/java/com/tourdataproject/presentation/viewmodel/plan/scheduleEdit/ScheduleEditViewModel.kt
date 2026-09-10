package com.tourdataproject.presentation.viewmodel.plan.scheduleEdit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEffect
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditIntent
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

    fun onIntent(intent: ScheduleEditIntent) {
        when (intent) {
            is ScheduleEditIntent.OnInit -> {
                _state.update {
                    it.copy(
                        dateLabel = intent.dateLabel,
                        schedules = intent.schedules,
                        stay = intent.stay
                    )
                }
            }
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

    private fun setDayNum(dayNum: Int){
        _state.update { currentState ->
            currentState.copy(dayNumber = dayNum)
        }
    }

    private fun deleteStay() {
        val stayId = _state.value.stay?.scheduleId ?: return
        _state.update { it.copy(stay = null) }
        viewModelScope.launch {
            _effect.emit(ScheduleEditEffect.DeleteStayFromShared(stayId))
        }
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
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // stay는 이 화면에서 순서를 건드리지 않으므로 schedules만 공유 뷰모델에 반영
            _effect.emit(ScheduleEditEffect.SaveToShared(_state.value.dayNumber, _state.value.schedules))

            _state.update { it.copy(isLoading = false) }
            _effect.emit(ScheduleEditEffect.ShowToast("일정이 저장되었습니다."))
            _effect.emit(ScheduleEditEffect.NavigateBack)
        }
    }
}
