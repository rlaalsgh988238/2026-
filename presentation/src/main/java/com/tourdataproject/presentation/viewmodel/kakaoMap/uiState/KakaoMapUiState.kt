package com.tourdataproject.presentation

import com.tourdataproject.presentation.model.KakaoMapPresentationModel
import com.tourdataproject.presentation.viewmodel.base.BaseState

data class KakaoMapState(
    val searchQuery: String = "",
    val targetCoordinate: Pair<Double, Double>? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchResults: List<KakaoMapPresentationModel> = emptyList(),
    val autoCompleteResults: List<KakaoMapPresentationModel> = emptyList(),

    override val entryPoint: String? = null,
    override val purpose: String? = null
): BaseState<KakaoMapState> {
    override fun setEntryPoint(entryPoint: String?): KakaoMapState =
        this.copy(
            entryPoint = entryPoint
        )

    override fun setPurpose(purpose: String?): KakaoMapState =
        this.copy(
            purpose = purpose
        )
}

sealed interface KakaoMapIntent {
    data class OnSearchQueryChanged(val query: String) : KakaoMapIntent
    data class OnSearchClicked(val query: String) : KakaoMapIntent

    data class OnPlaceItemClicked(val place: KakaoMapPresentationModel) : KakaoMapIntent
    data class OnInitLocation(val latitude: Double, val longitude: Double) : KakaoMapIntent
}

sealed interface KakaoMapEffect {
    data class ShowToast(val message: String) : KakaoMapEffect

    data class NavigateNextScreen(val place: KakaoMapPresentationModel) : KakaoMapEffect
}