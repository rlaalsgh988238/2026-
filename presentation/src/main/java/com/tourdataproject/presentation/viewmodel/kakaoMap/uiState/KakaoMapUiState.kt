package com.tourdataproject.presentation

import com.tourdataproject.presentation.model.KakaoMapPresentationModel

data class KakaoMapState(
    val searchQuery: String = "",
    val targetCoordinate: Pair<Double, Double>? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchResults: List<KakaoMapPresentationModel> = emptyList(),
    val autoCompleteResults: List<KakaoMapPresentationModel> = emptyList()
)

sealed interface KakaoMapEvent {
    data class OnSearchQueryChanged(val query: String) : KakaoMapEvent
    data class OnSearchClicked(val query: String) : KakaoMapEvent

    data class OnPlaceItemClicked(val place: KakaoMapPresentationModel) : KakaoMapEvent
    data class OnInitLocation(val latitude: Double, val longitude: Double) : KakaoMapEvent
}

sealed interface KakaoMapEffect {
    data class ShowToast(val message: String) : KakaoMapEffect

    data class NavigateNextScreen(val place: KakaoMapPresentationModel) : KakaoMapEffect
}