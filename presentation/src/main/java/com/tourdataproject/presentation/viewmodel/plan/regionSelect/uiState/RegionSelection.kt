package com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState

data class RegionSelectionState(
    val searchQuery: String = "",
    val popularCities: List<RegionPresentationModel> = emptyList(),
    val searchResults: List<RegionPresentationModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false
) {
    val isSearchMode: Boolean
        get() = searchQuery.isNotBlank()
}

sealed class RegionSelectionIntent {
    data class OnSearchQueryChanged(val query: String) : RegionSelectionIntent()
    object OnBackButtonClicked : RegionSelectionIntent()
}

sealed class RegionSelectionEffect {
    object NavigateBack : RegionSelectionEffect()
}
