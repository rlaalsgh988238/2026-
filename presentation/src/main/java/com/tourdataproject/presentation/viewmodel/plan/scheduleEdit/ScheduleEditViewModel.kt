package com.tourdataproject.presentation.viewmodel.plan.scheduleEdit

import android.util.Log
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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScheduleEditViewModel @Inject constructor(
    // TODO: UseCase 주입 필요
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleEditState())
    val state: StateFlow<ScheduleEditState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ScheduleEditEffect>()
    val effect: SharedFlow<ScheduleEditEffect> = _effect.asSharedFlow()

    init {
        loadDummyData()
    }

    fun setEvent(event: ScheduleEditEvent) {
        when (event) {
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

    private fun moveSchedule(fromIndex: Int, toIndex: Int) {
        Log.d("ScheduleEditVM", "일정 이동 요청: fromIndex=$fromIndex, toIndex=$toIndex")
        _state.update { currentState ->
            val mutableSchedules = currentState.schedules.toMutableList()

            // 숙소(마지막 아이템)는 이동 불가, 다른 아이템이 숙소 자리로 가는 것도 불가
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
            // TODO: SharedViewModel 또는 DB에 저장 로직 추가

            _state.update { it.copy(isLoading = false) }
            _effect.emit(ScheduleEditEffect.ShowToast("일정이 저장되었습니다."))
            _effect.emit(ScheduleEditEffect.NavigateBack)
        }
    }

    private fun loadDummyData() {
        val dummySchedules = listOf(
            ScheduleItemUiModel(scheduleId = UUID.randomUUID().toString(), order = 1, scheduleName = "가덕휴게소", latitude = 35.024, longitude = 128.825),
            ScheduleItemUiModel(scheduleId = UUID.randomUUID().toString(), order = 2, scheduleName = "매미성", latitude = 34.975, longitude = 128.718),
            ScheduleItemUiModel(scheduleId = UUID.randomUUID().toString(), order = 3, scheduleName = "바람의 언덕", latitude = 34.761, longitude = 128.659),
            ScheduleItemUiModel(scheduleId = UUID.randomUUID().toString(), order = 4, scheduleName = "거제 파노라마 케이블카", latitude = 34.801, longitude = 128.623),
            ScheduleItemUiModel(scheduleId = UUID.randomUUID().toString(), order = 5, scheduleName = "거제 YAHO HOTEL", latitude = 34.880, longitude = 128.621)
        )

        _state.value = ScheduleEditState(
            dayNumber = 1,
            dateLabel = "8/30 (일)",
            schedules = dummySchedules
        )
    }
}
