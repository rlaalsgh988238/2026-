package com.tourdataproject.map_presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
private const val TAG = "MapViewModel"

@HiltViewModel
class MapViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    fun searchPlace() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null // 🌟 새로운 검색 시작 시 기존 에러 메시지 초기화
            )

            try {
                Log.d(TAG, "검색어 : ${_uiState.value.searchQuery}")

                // TODO: REmote에서 나중에 받ㅈ아와서 넣기
                _uiState.value = _uiState.value.copy(
                    targetCoordinate = Pair(37.498095, 127.027610),
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    // 🌟 Exception 객체에서 에러 메시지를 뽑아서 전달
                    errorMessage = e.localizedMessage ?: "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }
}