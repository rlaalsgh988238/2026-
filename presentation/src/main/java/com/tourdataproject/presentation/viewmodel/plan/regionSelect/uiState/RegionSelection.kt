package com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState

data class RegionSelectionState(
    val searchQuery: String = "",
    val popularCities: List<RegionPresentationModel> = emptyList(),
    val searchResults: List<RegionPresentationModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,

    // 도시 수정 화면에서만 사용
    val editCourseId: String = "",
    val isEditInitialized: Boolean = false,
    val editSelectedCity: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val isSearchMode: Boolean
        get() = searchQuery.isNotBlank()
}

sealed class RegionSelectionIntent {
    data class OnSearchQueryChanged(
        val query: String
    ) : RegionSelectionIntent()

    object OnBackButtonClicked : RegionSelectionIntent()

    data class OnInitializeEdit(
        val courseId: String,
        val cityName: String
    ) : RegionSelectionIntent()

    data class OnEditCitySelected(
        val cityName: String
    ) : RegionSelectionIntent()

    object OnEditCityDeselected : RegionSelectionIntent()

    object OnEditNextClicked : RegionSelectionIntent()

    data class OnEditApplyCompleted(
        val success: Boolean
    ) : RegionSelectionIntent()

    object OnRetryPopularCities : RegionSelectionIntent()
}

sealed class RegionSelectionEffect {
    object NavigateBack : RegionSelectionEffect()

    data class ApplyEditedDestination(
        val courseId: String,
        val cityName: String,
        val latitude: Double,
        val longitude: Double
    ) : RegionSelectionEffect()
}
