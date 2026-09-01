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
import javax.inject.Inject

@HiltViewModel
class DateSelectionViewModel @Inject constructor() : ViewModel() {

    // 화면에 보여줄 상태 (State)
    private val _state = MutableStateFlow(DateSelectionState())
    val state: StateFlow<DateSelectionState> = _state.asStateFlow()

    // 단발성 이벤트 (Effect - 화면 이동, 토스트 메시지 등)
    private val _effect = MutableSharedFlow<DateSelectionEffect>()
    val effect: SharedFlow<DateSelectionEffect> = _effect.asSharedFlow()

    // UI에서 발생하는 이벤트를 처리하는 함수
    fun setEvent(event: DateSelectionEvent) {
        when (event) {
            is DateSelectionEvent.OnDateSelected -> {
                _state.update { currentState ->
                    currentState.copy(selectedDate = event.date)
                }
            }
            is DateSelectionEvent.OnNextButtonClicked -> {
                viewModelScope.launch {
                    _effect.emit(DateSelectionEffect.NavigateToNextScreen)
                }
            }
            is DateSelectionEvent.OnBackButtonClicked -> {
                viewModelScope.launch {
                    _effect.emit(DateSelectionEffect.NavigateBack)
                }
            }
        }
    }
}