package com.tourdataproject.map_data.model

data class KakaoMapDataModel(
    val id: String,
    val placeName: String,
    val addressName: String,
    val roadAddressName: String,
    val longitude: Double,
    val latitude: Double,
    val distance: Int,
    val categoryGroupName: String,
    val phone: String,
    val placeUrl: String
)