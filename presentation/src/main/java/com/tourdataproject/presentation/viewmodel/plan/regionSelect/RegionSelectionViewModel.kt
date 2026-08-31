package com.tourdataproject.presentation.viewmodel.plan.regionSelect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.usecase.plan.GetPopularCitiesUseCase
import com.tourdataproject.presentation.model.RegionUiModel
import com.tourdataproject.presentation.model.toUiModel
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEvent
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionState
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
class RegionSelectionViewModel @Inject constructor(
    private val getPopularCitiesUseCase: GetPopularCitiesUseCase
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
                _state.update { it.copy(searchQuery = event.query) }
            }
            is RegionSelectionEvent.OnCitySelected -> {
                _state.update { it.copy(selectedCity = event.city) }
            }
            is RegionSelectionEvent.OnCityDeselected -> {
                _state.update { it.copy(selectedCity = null) }
            }
            is RegionSelectionEvent.OnNextButtonClicked -> {
                val selectedCity = _state.value.selectedCity ?: return
                viewModelScope.launch {
                    _effect.emit(RegionSelectionEffect.NavigateToDateSelection(selectedCity.code))
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
        viewModelScope.launch {
            getPopularCitiesUseCase().collect { resource ->
                Log.d("RegionSelection", "Resource State: ${resource::class.java.simpleName}")

                when (resource) {
                    is DataResource.Success -> {
                        Log.d("RegionSelection", "Data Size: ${resource.data.size}") // 데이터 개수 확인
                        val uiModels = resource.data.map { it.toUiModel() }
                        _state.update { it.copy(popularCities = uiModels, isLoading = false) }
                    }
                    is DataResource.Error -> {
                        Log.e("RegionSelection", "Error: ${resource.throwable.message}")
                    }
                    is DataResource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }


    private fun loadDummyPopularCities() {
        val dummyCities = createCityList(
            "서울", "대전", "청주", "인천", "수원", "대구",
            "부산", "전주", "광주", "나주", "제주", "거제"
        )
        _state.value = _state.value.copy(popularCities = dummyCities)
    }

    private fun createCityList(vararg names: String): List<RegionUiModel> =
        names.mapIndexed { index, name -> RegionUiModel((index + 1).toString(), name) }
}