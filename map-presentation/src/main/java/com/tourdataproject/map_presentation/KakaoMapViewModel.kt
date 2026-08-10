package com.tourdataproject.map_presentation

import androidx.lifecycle.ViewModel
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.usecase.SearchNearbyPlacesUseCase
import com.tourdataproject.map_presentation.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import javax.inject.Inject
import org.orbitmvi.orbit.viewmodel.container


@HiltViewModel
class KakaoMapViewModel @Inject constructor(
    private val searchNearbyPlacesUseCase: SearchNearbyPlacesUseCase
) : ViewModel(), ContainerHost<KakaoMapUiState, KakaoMapSideEffect> {

    // Orbit 컨테이너 초기화
    override val container = container<KakaoMapUiState, KakaoMapSideEffect>(KakaoMapUiState())

    // 유저가 검색창에 타이핑할 때마다 State 업데이트 (검색창 글자 유지용)
    fun updateSearchQuery(query: String) = intent {
        reduce { state.copy(searchQuery = query) }
    }

    // 실제 검색 버튼을 눌렀을 때 실행되는 함수 ->screen에 넣기
    fun searchPlaces(query: String, longitude: Double? = null, latitude: Double? = null) = intent {
        if (query.isBlank()) {
            postSideEffect(KakaoMapSideEffect.ShowToast("검색어를 입력해주세요."))
            return@intent
        }

        //로딩(통신 start)
        reduce { state.copy(isLoading = true, errorMessage = null) }

        // 2. UseCase 호출
        //TODO : local에서 자기 좌표값 가져온느거?
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
                            searchResults = uiModels
                        )
                    }
                }

                is DataResource.Error -> {
                    reduce { state.copy(isLoading = false) }
                    val errorMsg = resource.throwable.message ?: "검색 중 오류가 발생했습니다."
                    postSideEffect(KakaoMapSideEffect.ShowToast(errorMsg))
                }

                is DataResource.Loading -> {
                    //TODO:  로딩 화면 만들어서 넣기
                }
            }
        }
    }
}