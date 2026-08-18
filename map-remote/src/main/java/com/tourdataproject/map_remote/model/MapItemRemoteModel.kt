package com.tourdataproject.map_remote.model

data class MapItemRemoteModel(
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