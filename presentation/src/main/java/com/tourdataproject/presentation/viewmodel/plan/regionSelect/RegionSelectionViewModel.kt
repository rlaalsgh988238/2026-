package com.tourdataproject.presentation.viewmodel.plan.regionSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.usecase.plan.GetPopularCitiesUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionByKeywordUseCase
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.toUiModel
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionIntent
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegionSelectionViewModel @Inject constructor(
    private val getPopularCitiesUseCase: GetPopularCitiesUseCase,
    private val getRegionByKeywordUseCase: GetRegionByKeywordUseCase
) : ViewModel() {

    private val TAG = "RegionSelection"

    private val _state = MutableStateFlow(RegionSelectionState())
    val state: StateFlow<RegionSelectionState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<RegionSelectionEffect>()
    val effect: SharedFlow<RegionSelectionEffect> = _effect.asSharedFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadPopularCities()
        observeSearchQuery()
    }

    fun onIntent(intent: RegionSelectionIntent) {
        when (intent) {
            is RegionSelectionIntent.OnSearchQueryChanged -> handleSearchQueryChanged(intent.query)
            is RegionSelectionIntent.OnBackButtonClicked -> handleBackButtonClicked()
        }
    }

    private fun handleSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    private fun handleBackButtonClicked() {
        viewModelScope.launch {
            _effect.emit(RegionSelectionEffect.NavigateBack)
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        flowOf(DataResource.Success(emptyList()))
                    } else {
                        _state.update { it.copy(isSearching = true) }
                        getRegionByKeywordUseCase(query)
                    }
                }
                .collect { resource ->
                    when (resource) {
                        is DataResource.Success -> {
                            val results = resource.data.map { it.toUiModel() }
                            _state.update {
                                it.copy(searchResults = results, isSearching = false)
                            }
                        }
                        is DataResource.Error -> {
                            Log.e(TAG, "Search error: ${resource.throwable.message}")
                            _state.update {
                                it.copy(searchResults = emptyList(), isSearching = false)
                            }
                        }
                        is DataResource.Loading -> {
                            _state.update { it.copy(isSearching = true) }
                        }
                    }
                }
        }
    }

    private fun loadPopularCities() {
        viewModelScope.launch {
            getPopularCitiesUseCase().collect { resource ->
                when (resource) {
                    is DataResource.Success -> {
                        val uiModels = resource.data.map { it.toUiModel() }
                        _state.update { it.copy(popularCities = uiModels, isLoading = false) }
                    }
                    is DataResource.Error -> {
                        Log.e(TAG, "Error: ${resource.throwable.message}")
                        _state.update { it.copy(isLoading = false) }
                    }
                    is DataResource.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }
}
