package com.tourdataproject.map_presentation.model.region

data class City(val id: Int, val name: String)

data class RegionSelectionState(
    val searchQuery: String = "",
    val popularCities: List<City> = emptyList(),
    val selectedCity: City? = null,
    val isLoading: Boolean = false
) {
    // 선택된 도시가 있을 때만 다음 버튼 활성화
    val isNextButtonEnabled: Boolean
        get() = selectedCity != null
}

sealed class RegionSelectionEvent {
    data class OnSearchQueryChanged(val query: String) : RegionSelectionEvent()
    data class OnCitySelected(val city: City) : RegionSelectionEvent()
    object OnCityDeselected : RegionSelectionEvent()
    object OnNextButtonClicked : RegionSelectionEvent()
    object OnBackButtonClicked : RegionSelectionEvent()
}

sealed class RegionSelectionEffect {
    data class NavigateToDateSelection(val selectedCityId: Int) : RegionSelectionEffect()
    object NavigateBack : RegionSelectionEffect()
}
