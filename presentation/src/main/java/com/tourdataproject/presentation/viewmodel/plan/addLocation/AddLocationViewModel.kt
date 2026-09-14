package com.tourdataproject.presentation.viewmodel.plan.addLocation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.base.BaseViewModel
import com.tourdataproject.presentation.viewmodel.plan.addLocation.uiState.AddLocationIntent
import com.tourdataproject.presentation.viewmodel.plan.addLocation.uiState.AddLocationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddLocationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<AddLocationState>(
    savedStateHandle,
    AddLocationState()
) {

    fun onIntent(intent: AddLocationIntent) {
        when (intent) {
            else -> {}
        }
    }
}