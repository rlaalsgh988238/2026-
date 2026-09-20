package com.tourdataproject.presentation.viewmodel.plan.regionSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.usecase.plan.GetPopularCitiesUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionByKeywordUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionIntent
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionState
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegionSelectionViewModel @Inject constructor(
    private val getPopularCitiesUseCase: GetPopularCitiesUseCase,
    private val getRegionByKeywordUseCase: GetRegionByKeywordUseCase,
    private val getRegionPositionUseCase: GetRegionPositionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegionSelectionState())
    val state = _state.asStateFlow()

    private val _effect = Channel<RegionSelectionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private var popularCitiesJob: Job? = null

    init {
        loadPopularCities()
        observeSearchQuery()
    }

    fun onIntent(intent: RegionSelectionIntent) {
        when (intent) {
            is RegionSelectionIntent.OnSearchQueryChanged -> {
                changeSearchQuery(intent.query)
            }

            RegionSelectionIntent.OnBackButtonClicked -> {
                if (!_state.value.isSubmitting) {
                    viewModelScope.launch {
                        _effect.send(RegionSelectionEffect.NavigateBack)
                    }
                }
            }

            is RegionSelectionIntent.OnInitializeEdit -> {
                initializeEdit(
                    courseId = intent.courseId,
                    cityName = intent.cityName
                )
            }

            is RegionSelectionIntent.OnEditCitySelected -> {
                selectEditCity(intent.cityName)
            }

            RegionSelectionIntent.OnEditCityDeselected -> {
                if (!_state.value.isSubmitting) {
                    _state.update {
                        it.copy(
                            editSelectedCity = "",
                            errorMessage = null
                        )
                    }
                }
            }

            RegionSelectionIntent.OnEditNextClicked -> {
                submitEditCity()
            }

            is RegionSelectionIntent.OnEditApplyCompleted -> {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = if (intent.success) {
                            null
                        } else {
                            "여행 정보를 반영하지 못했습니다. 다시 열어주세요."
                        }
                    )
                }
            }

            RegionSelectionIntent.OnRetryPopularCities -> {
                loadPopularCities()
            }
        }
    }

    private fun initializeEdit(
        courseId: String,
        cityName: String
    ) {
        if (courseId.isBlank()) return

        val current = _state.value

        // 같은 편집 화면에서 공유 데이터가 갱신되더라도
        // 사용자가 선택 중인 도시를 다시 덮어쓰지 않음.
        if (
            current.isEditInitialized &&
            current.editCourseId == courseId
        ) {
            return
        }

        _state.update {
            it.copy(
                editCourseId = courseId,
                isEditInitialized = true,
                editSelectedCity = cityName,
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false,
                errorMessage = null
            )
        }

        searchQueryFlow.value = ""
    }

    private fun changeSearchQuery(query: String) {
        if (_state.value.isSubmitting) return

        _state.update {
            it.copy(
                searchQuery = query,
                searchResults = emptyList(),
                isSearching = query.isNotBlank(),
                errorMessage = null
            )
        }

        searchQueryFlow.value = query
    }

    private fun selectEditCity(cityName: String) {
        if (_state.value.isSubmitting) return

        _state.update {
            it.copy(
                editSelectedCity = cityName,
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false,
                errorMessage = null
            )
        }

        searchQueryFlow.value = ""
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow.collectLatest { rawQuery ->
                val keyword = rawQuery.trim()

                if (keyword.isBlank()) {
                    _state.update {
                        it.copy(
                            searchResults = emptyList(),
                            isSearching = false
                        )
                    }
                    return@collectLatest
                }

                // 검색어가 변경되면 이전 요청을 취소하고
                // 마지막 입력에서 300ms 후 검색.
                delay(300L)

                try {
                    getRegionByKeywordUseCase(keyword).collect { resource ->
                        if (_state.value.searchQuery != rawQuery) {
                            return@collect
                        }

                        when (resource) {
                            is DataResource.Loading -> {
                                _state.update {
                                    it.copy(isSearching = true)
                                }
                            }

                            is DataResource.Success -> {
                                _state.update {
                                    it.copy(
                                        searchResults = resource.data.map { region ->
                                            region.toUiModel()
                                        },
                                        isSearching = false
                                    )
                                }
                            }

                            is DataResource.Error -> {
                                _state.update {
                                    it.copy(
                                        searchResults = emptyList(),
                                        isSearching = false,
                                        errorMessage = "도시 검색에 실패했습니다."
                                    )
                                }
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (_state.value.searchQuery == rawQuery) {
                        _state.update {
                            it.copy(
                                searchResults = emptyList(),
                                isSearching = false,
                                errorMessage = "도시 검색에 실패했습니다."
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadPopularCities() {
        popularCitiesJob?.cancel()

        popularCitiesJob = viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }

            try {
                getPopularCitiesUseCase().collect { resource ->
                    when (resource) {
                        is DataResource.Loading -> {
                            _state.update {
                                it.copy(isLoading = true)
                            }
                        }

                        is DataResource.Success -> {
                            _state.update {
                                it.copy(
                                    popularCities = resource.data.map { region ->
                                        region.toUiModel()
                                    },
                                    isLoading = false
                                )
                            }
                        }

                        is DataResource.Error -> {
                            _state.update {
                                it.copy(isLoading = false)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun submitEditCity() {
        val current = _state.value

        if (!current.isEditInitialized) return
        if (current.editSelectedCity.isBlank()) return
        if (current.isSubmitting) return

        _state.update {
            it.copy(
                isSubmitting = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val result = getRegionPositionUseCase(
                    current.editSelectedCity
                ).first { resource ->
                    resource !is DataResource.Loading
                }

                when (result) {
                    is DataResource.Success -> {
                        _effect.send(
                            RegionSelectionEffect.ApplyEditedDestination(
                                courseId = current.editCourseId,
                                cityName = current.editSelectedCity,
                                latitude = result.data.latitude,
                                longitude = result.data.longitude
                            )
                        )
                    }

                    is DataResource.Error -> {
                        Log.e(
                            "RegionSelection",
                            "도시 좌표 조회 실패: " +
                                    "city=${current.editSelectedCity}, " +
                                    "type=${result.throwable.javaClass.simpleName}, " +
                                    "message=${result.throwable.message}"
                        )
                        showSubmitError()
                    }

                    is DataResource.Loading -> Unit
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    "RegionSelection",
                    "도시 변경 처리 실패: " +
                            "city=${current.editSelectedCity}, " +
                            "type=${e.javaClass.simpleName}, " +
                            "message=${e.message}"
                )
                showSubmitError()
            }

        }
    }

    private fun showSubmitError() {
        _state.update {
            it.copy(
                isSubmitting = false,
                errorMessage = "도시 위치를 확인하지 못했습니다. 다시 시도해주세요."
            )
        }
    }
}
