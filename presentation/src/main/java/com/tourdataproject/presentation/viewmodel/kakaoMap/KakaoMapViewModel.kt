package com.tourdataproject.presentation.viewmodel.kakaoMap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.usecase.SearchNearbyPlacesUseCase
import com.tourdataproject.presentation.KakaoMapEffect
import com.tourdataproject.presentation.KakaoMapIntent
import com.tourdataproject.presentation.KakaoMapState
import com.tourdataproject.presentation.mapper.toUiModel
import com.tourdataproject.presentation.model.KakaoMapPresentationModel
import com.tourdataproject.presentation.viewmodel.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class KakaoMapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase
) : BaseViewModel<KakaoMapState>(
    savedStateHandle = savedStateHandle,
    initialState = KakaoMapState()
), ContainerHost<KakaoMapState, KakaoMapEffect> {

    override val container = container<KakaoMapState, KakaoMapEffect>(KakaoMapState())
    private val queryFlow = MutableStateFlow("")
    private var autoCompleteJob: Job? = null

    init {
        observeQueryForAutoComplete()
    }

    fun onIntent(event: KakaoMapIntent) {
        when (event) {
            is KakaoMapIntent.OnSearchQueryChanged -> updateSearchQuery(event.query)
            is KakaoMapIntent.OnSearchClicked -> searchPlaces(event.query)
            is KakaoMapIntent.OnPlaceItemClicked -> selectPlace(event.place)
            is KakaoMapIntent.OnInitLocation -> intent {
                reduce {
                    state.copy(targetCoordinate = Pair(event.latitude, event.longitude))
                }
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        intent {
            reduce {
                state.copy(
                    searchQuery = query,
                    autoCompleteResults = if (query.isBlank()) emptyList() else state.autoCompleteResults
                )
            }
        }
        queryFlow.value = query
    }

    @OptIn(FlowPreview::class)
    private fun observeQueryForAutoComplete() {
        queryFlow
            .debounce(150L.milliseconds)
            .onEach { query ->
                autoCompleteJob?.cancel()

                if (query.isBlank()) {
                    intent {
                        reduce { state.copy(autoCompleteResults = emptyList()) }
                    }
                } else {
                    searchPlacesForAutoComplete(query)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun searchPlacesForAutoComplete(query: String) {
        autoCompleteJob = viewModelScope.launch {
            val coordinate = container.stateFlow.value.targetCoordinate ?: return@launch
            val currentLat = coordinate.first
            val currentLng = coordinate.second

            try {
                // UseCase 대신 Repository 직접 호출로 빠른 응답
                searchNearbyPlacesUseCase(
                    query = query,
                    longitude = currentLng,
                    latitude = currentLat,
                    radius = 20000,
                    page = 1,
                    includeGlobalSearch = false // 주변 검색만
                ).collect { resource ->
                    when (resource) {
                        is DataResource.Success -> {
                            val uiModels = resource.data.map { it.toUiModel() }
                            intent {
                                reduce { state.copy(autoCompleteResults = uiModels) }
                            }
                        }
                        is DataResource.Error -> {
                            // 자동완성 에러 무시
                        }
                        is DataResource.Loading -> {
                            // 로딩 무시
                        }
                    }
                }
            } catch (e: Exception) {
                // 취소 예외 무시
            }
        }
    }

    private fun searchPlaces(query: String, longitude: Double? = null, latitude: Double? = null) = intent {
        if (query.isBlank()) {
            postSideEffect(KakaoMapEffect.ShowToast("검색어를 입력해주세요."))
            return@intent
        }

        val targetLng = longitude ?: state.targetCoordinate?.second
        val targetLat = latitude ?: state.targetCoordinate?.first

        if (targetLng == null || targetLat == null) {
            postSideEffect(KakaoMapEffect.ShowToast("여행지 위치 정보가 없습니다. 이전 화면에서 다시 시도해주세요."))
            return@intent
        }

        reduce { state.copy(isLoading = true, errorMessage = null) }

        try {
            searchNearbyPlacesUseCase(
                query = query,
                longitude = targetLng,
                latitude = targetLat,
                radius = 20000,
                page = 1,
                includeGlobalSearch = true // 전국 + 주변 검색
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
                        postSideEffect(KakaoMapEffect.ShowToast(errorMsg))
                    }
                    is DataResource.Loading -> {
                        reduce { state.copy(isLoading = true) }
                    }
                }
            }
        } catch (e: Exception) {
            reduce { state.copy(isLoading = false) }
            postSideEffect(KakaoMapEffect.ShowToast("통신 중 예외가 발생했습니다."))
        }
    }

    private fun selectPlace(place: KakaoMapPresentationModel) = intent {
        postSideEffect(KakaoMapEffect.NavigateNextScreen(place))
    }
}
