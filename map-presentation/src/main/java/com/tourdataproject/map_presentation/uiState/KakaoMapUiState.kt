package com.tourdataproject.map_presentation

import com.tourdataproject.map_presentation.model.KakaoMapUiModel

data class KakaoMapUiState(
    val searchQuery: String = "",
    val targetCoordinate: Pair<Double, Double>? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchResults: List<KakaoMapUiModel> = emptyList()
)
//근데 SideEffect 가 있나 흠
sealed class KakaoMapSideEffect {
    data class ShowToast(val message: String) : KakaoMapSideEffect()
    // 유저가 검색 결과를 클릭하면, 그 좌표를 들고 메인 지도로 돌아가기 위한 액션
    data class NavigateBackToMap(val x: Double, val y: Double) : KakaoMapSideEffect()
}