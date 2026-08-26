package com.tourdataproject.presentation.viewmodel.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.model.region.City
import com.tourdataproject.presentation.model.region.RegionSelectionEffect
import com.tourdataproject.presentation.model.region.RegionSelectionEvent
import com.tourdataproject.presentation.model.region.RegionSelectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// RegionSelectionViewModel.kt
@HiltViewModel
class RegionSelectionViewModel @Inject constructor(
    // private val getPopularCitiesUseCase: GetPopularCitiesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegionSelectionState())
    val state: StateFlow<RegionSelectionState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RegionSelectionEffect>()
    val effect: SharedFlow<RegionSelectionEffect> = _effect.asSharedFlow()

    init {
        loadPopularCities()
    }

    fun setEvent(event: RegionSelectionEvent) {
        when (event) {
            is RegionSelectionEvent.OnSearchQueryChanged -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                // 필요하다면 여기서 검색 API 호출 로직 추가
            }
            is RegionSelectionEvent.OnCitySelected -> {
                _state.value = _state.value.copy(selectedCity = event.city)
            }
            is RegionSelectionEvent.OnCityDeselected -> {
                _state.value = _state.value.copy(selectedCity = null)
            }
            is RegionSelectionEvent.OnNextButtonClicked -> {
                val selectedCity = _state.value.selectedCity ?: return
                viewModelScope.launch {
                    _effect.emit(RegionSelectionEffect.NavigateToDateSelection(selectedCity.id))
                }
            }
            is RegionSelectionEvent.OnBackButtonClicked -> {
                viewModelScope.launch {
                    _effect.emit(RegionSelectionEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadPopularCities() {
        val dummyCities = listOf(
            City(1, "서울"), City(2, "대전"), City(3, "청주"), City(4, "인천"),
            City(5, "수원"), City(6, "대구"), City(7, "부산"), City(8, "전주"),
            City(9, "광주"), City(10, "나주"), City(11, "제주"), City(12, "거제")
        )
        _state.value = _state.value.copy(popularCities = dummyCities)
    }
}