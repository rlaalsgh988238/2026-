package com.tourdataproject.presentation.model

data class KakaoMapUiModel(
    val id: String,
    val placeName: String,
    val address: String,
    val distanceText: String,    // "1.5km" 처럼 예쁘게 포장된 문자열
    val x: Double,
    val y: Double,
    val category: String,
    val phone: String
)