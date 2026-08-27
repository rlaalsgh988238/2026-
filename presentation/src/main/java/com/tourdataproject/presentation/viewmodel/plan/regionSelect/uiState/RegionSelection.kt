package com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState

import com.tourdataproject.presentation.model.RegionUiModel

data class RegionSelectionState(
    val searchQuery: String = "",
    val popularCities: List<RegionUiModel> = emptyList(),
    val selectedCity: RegionUiModel? = null,
    val isLoading: Boolean = false
) {
    // 선택된 도시가 있을 때만 다음 버튼 활성화
    val isNextButtonEnabled: Boolean
        get() = selectedCity != null
}

sealed class RegionSelectionEvent {
    data class OnSearchQueryChanged(val query: String) : RegionSelectionEvent()
    data class OnCitySelected(val city: RegionUiModel) : RegionSelectionEvent()
    object OnCityDeselected : RegionSelectionEvent()
    object OnNextButtonClicked : RegionSelectionEvent()
    object OnBackButtonClicked : RegionSelectionEvent()
}

sealed class RegionSelectionEffect {
    data class NavigateToDateSelection(val selectedCityId: String) : RegionSelectionEffect()
    object NavigateBack : RegionSelectionEffect()
}