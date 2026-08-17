package com.tourdataproject.map_presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.usecase.SearchNearbyPlacesUseCase
import com.tourdataproject.map_presentation.KakaoMapSideEffect
import com.tourdataproject.map_presentation.KakaoMapUiState
import com.tourdataproject.map_presentation.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class KakaoMapViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase
) : ViewModel(), ContainerHost<KakaoMapUiState, KakaoMapSideEffect> {

    override val container = container<KakaoMapUiState, KakaoMapSideEffect>(KakaoMapUiState())

    private val queryFlow = MutableStateFlow("")

    init {
        observeQueryForAutoComplete()
    }

    fun updateSearchQuery(query: String) = intent {
        reduce {
            state.copy(
                searchQuery = query,
                autoCompleteResults = if (query.isBlank()) emptyList() else state.autoCompleteResults
            )
        }
        queryFlow.value = query
    }

    @OptIn(FlowPreview::class)
    private fun observeQueryForAutoComplete() {
        queryFlow
            .debounce(300L.milliseconds)
            .filter { it.isNotBlank() }
            .onEach { finalQuery ->
                searchPlacesForAutoComplete(finalQuery)
            }
            .launchIn(viewModelScope)
    }

    private fun searchPlacesForAutoComplete(query: String) = intent {
        searchNearbyPlacesUseCase(
            query = query,
            longitude = null,
            latitude = null,
            radius = null,
            page = 1
        ).collect { resource ->
            if (resource is DataResource.Success) {
                //TODO: 예외일 때 로직
                val uiModels = resource.data.map { it.toUiModel() }

                reduce {
                    state.copy(autoCompleteResults = uiModels)
                }
            }
        }
    }

    fun searchPlaces(query: String, longitude: Double? = null, latitude: Double? = null) = intent {
        if (query.isBlank()) {
            postSideEffect(KakaoMapSideEffect.ShowToast("검색어를 입력해주세요."))
            return@intent
        }

        reduce { state.copy(isLoading = true, errorMessage = null) }

        val radius = if (longitude != null && latitude != null) 2000 else null

        searchNearbyPlacesUseCase(
            query = query,
            longitude = longitude,
            latitude = latitude,
            radius = radius,
            page = 1
        ).collect { resource ->
            when (resource) {
                is DataResource.Success -> {
                    val uiModels = resource.data.map { it.toUiModel() }

                    reduce {
                        state.copy(
                            isLoading = false,
                            searchResults = uiModels,
                            autoCompleteResults = emptyList()
                        )
                    }
                }

                is DataResource.Error -> {
                    reduce { state.copy(isLoading = false) }
                    val errorMsg = resource.throwable.message ?: "검색 중 오류가 발생했습니다."
                    postSideEffect(KakaoMapSideEffect.ShowToast(errorMsg))
                }

                is DataResource.Loading -> {
                    // 필요한 경우 처리
                }
            }
        }
    }

    fun selectPlace(x: Double, y: Double) = intent {
        postSideEffect(KakaoMapSideEffect.NavigateBackToMap(x, y))
    }
}