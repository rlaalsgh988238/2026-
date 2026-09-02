package com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState

import com.tourdataproject.presentation.model.RegionUiModel

data class RegionSelectionState(
    val searchQuery: String = "",
    val popularCities: List<RegionUiModel> = emptyList(),
    val searchResults: List<RegionUiModel> = emptyList(),
    val selectedCity: RegionUiModel? = null,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false
) {
    val isNextButtonEnabled: Boolean
        get() = selectedCity != null

    // 검색어가 있으면 검색 모드로 간주
    val isSearchMode: Boolean
        get() = searchQuery.isNotBlank()
}

sealed class RegionSelectionEvent {
    data class OnSearchQueryChanged(val query: String) : RegionSelectionEvent()
    data class OnCitySelected(val city: RegionUiModel) : RegionSelectionEvent()
    object OnCityDeselected : RegionSelectionEvent()
    object OnNextButtonClicked : RegionSelectionEvent()
    object OnBackButtonClicked : RegionSelectionEvent()
}

sealed class RegionSelectionEffect {
    data class NavigateToDateSelection(val regionName: String) : RegionSelectionEffect()
    object NavigateBack : RegionSelectionEffect()
}