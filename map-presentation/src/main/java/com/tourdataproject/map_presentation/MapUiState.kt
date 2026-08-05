package com.tourdataproject.map_presentation

data class MapUiState(
    val searchQuery: String = "",
    val targetCoordinate: Pair<Double, Double>? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)