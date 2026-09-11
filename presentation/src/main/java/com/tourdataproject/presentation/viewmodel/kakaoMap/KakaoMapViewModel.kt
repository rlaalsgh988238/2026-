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
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.base.BaseViewModel
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
    savedStateHandle: SavedStateHandle,
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase
) : BaseViewModel<KakaoMapState>(
    savedStateHandle = savedStateHandle,
    initialState = KakaoMapState()
), ContainerHost<KakaoMapState, KakaoMapEffect> {

    override val container = container<KakaoMapState, KakaoMapEffect>(KakaoMapState())
    private val queryFlow = MutableStateFlow("")

    init {
        observeQueryForAutoComplete()
    }

    fun onIntent(event: KakaoMapIntent) {
        Log.d("KakaoMapDebug", "Event received: $event")
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

    private fun updateSearchQuery(query: String) = intent {
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
        // 🌟 1. 자동완성은 사용자가 타이핑할 때마다 호출되므로, 좌표가 없으면 토스트 없이 조용히 무시(return)합니다.
        val coordinate = state.targetCoordinate ?: return@intent
        val currentLat = coordinate.first
        val currentLng = coordinate.second

        searchNearbyPlacesUseCase(
            query = query,
            longitude = currentLng,
            latitude = currentLat,
            radius = 20000,
            page = 1
        ).collect { resource ->
            if (resource is DataResource.Success) {
                val uiModels = resource.data.map { it.toUiModel() }
                reduce { state.copy(autoCompleteResults = uiModels) }
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

        Log.d("KakaoMapDebug", "1. searchPlaces 시작: query = $query")
        reduce { state.copy(isLoading = true, errorMessage = null) }
        val radius = 20000

        try {
            Log.d("KakaoMapDebug", "2. UseCase 호출 직전")

            
            searchNearbyPlacesUseCase(
                query = query,
                longitude = targetLng,
                latitude = targetLat,
                radius = radius,
                page = 1
            ).collect { resource ->
                android.util.Log.d("KakaoMapDebug", "3. UseCase 응답 도착! resource = $resource")

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
                        android.util.Log.e("KakaoMapDebug", "4. Error 발생: ${resource.throwable.message}")
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
            android.util.Log.e("KakaoMapDebug", "6. 예외 터짐(Catch): ${e.localizedMessage}", e)
            reduce { state.copy(isLoading = false) }
            postSideEffect(KakaoMapEffect.ShowToast("통신 중 예외가 발생했습니다."))
        }
    }
    private fun selectPlace(place: KakaoMapPresentationModel) = intent {
        postSideEffect(KakaoMapEffect.NavigateNextScreen(place))
    }
}
