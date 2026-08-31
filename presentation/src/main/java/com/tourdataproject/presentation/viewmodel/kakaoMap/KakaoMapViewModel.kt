package com.tourdataproject.presentation.viewmodel.kakaoMap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.usecase.SearchNearbyPlacesUseCase
import com.tourdataproject.presentation.KakaoMapEffect
import com.tourdataproject.presentation.KakaoMapEvent
import com.tourdataproject.presentation.KakaoMapState
import com.tourdataproject.presentation.mapper.toUiModel
import com.tourdataproject.presentation.model.KakaoMapUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject
import kotlin.collections.map
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class KakaoMapViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase
) : ViewModel(), ContainerHost<KakaoMapState, KakaoMapEffect> {

    override val container = container<KakaoMapState, KakaoMapEffect>(KakaoMapState())

    private val queryFlow = MutableStateFlow("")
    val longitude = 126.9723 // 임시 경도 (서울역)
    val latitude = 37.5546   // 임시 위도 (서울역)
    init {
        observeQueryForAutoComplete()
    }

    fun onEvent(event: KakaoMapEvent) {
        android.util.Log.d("KakaoMapDebug", "Event received: $event")
        when (event) {
            is KakaoMapEvent.OnSearchQueryChanged -> updateSearchQuery(event.query)
            is KakaoMapEvent.OnSearchClicked -> searchPlaces(event.query)
            // 🌟 event에서 place를 꺼내서 넘겨줌
            is KakaoMapEvent.OnPlaceItemClicked -> selectPlace(event.place)
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
        searchNearbyPlacesUseCase(
            query = query,
            longitude = longitude,
            latitude = latitude,
            radius = null,
            page = 1
        ).collect { resource ->
            if (resource is DataResource.Success) {
                val uiModels = resource.data.map { it.toUiModel() }
                reduce {
                    state.copy(autoCompleteResults = uiModels)
                }
            }
        }
    }

    private fun searchPlaces(query: String, longitude: Double? = null, latitude: Double? = null) = intent {
        if (query.isBlank()) {
            postSideEffect(KakaoMapEffect.ShowToast("검색어를 입력해주세요."))
            return@intent
        }

        android.util.Log.d("KakaoMapDebug", "1. searchPlaces 시작: query = $query")
        reduce { state.copy(isLoading = true, errorMessage = null) }

        val targetLng = longitude ?: 126.9780
        val targetLat = latitude ?: 37.5665
        val radius = 20000

        try {
            android.util.Log.d("KakaoMapDebug", "2. UseCase 호출 직전")

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


    private fun selectPlace(place: KakaoMapUiModel) = intent {
        postSideEffect(KakaoMapEffect.NavigateNextScreen(place))
    }
}
