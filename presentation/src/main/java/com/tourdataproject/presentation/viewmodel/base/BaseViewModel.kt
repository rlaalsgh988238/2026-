package com.tourdataproject.presentation.viewmodel.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tourdataproject.presentation.utility.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State : BaseState<State>>(
    savedStateHandle: SavedStateHandle,
    initialState: State
) : ViewModel(){

    protected val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        val purpose = savedStateHandle.get<String>("purpose")
        val from = savedStateHandle.get<String>("from")
        _state.update { it.setPurpose(purpose) }
        _state.update { it.setEntryPoint(from) }
        Log.d("BaseViewModel", "진입점: ${state.value.entryPoint}")
        Log.d("BaseViewModel", "목표: ${state.value.purpose}")
    }
}