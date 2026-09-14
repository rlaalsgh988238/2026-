package com.tourdataproject.presentation.viewmodel.toilet.uiState

data class ToiletItemPresentationModel(
    val name: String,
    val address: String,
    val distance: String,
    val lat: Double,
    val lng: Double
)

data class NearbyToiletState(
    val isLoading: Boolean = false,
    val currentLocation: Pair<Double, Double>? = null,
    val toilets: List<ToiletItemPresentationModel> = emptyList(),
    val errorMessage: String? = null
)

sealed class NearbyToiletIntent {
    object LoadToilets : NearbyToiletIntent()
    data class OnToiletGuideClicked(val targetToilet: ToiletItemPresentationModel) : NearbyToiletIntent()
    object OnBackClicked : NearbyToiletIntent()
}

sealed class NearbyToiletEffect {
    object NavigateBack : NearbyToiletEffect()
    data class ShowToast(val message: String) : NearbyToiletEffect()
    data class NavigateToExternalMap(
        val startLat: Double,
        val startLng: Double,
        val destLat: Double,
        val destLng: Double
    ) : NearbyToiletEffect()
}