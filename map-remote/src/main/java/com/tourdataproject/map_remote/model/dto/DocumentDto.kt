package com.tourdataproject.map_remote.model.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DocumentDto(
    @SerializedName("id") val id: String,
    @SerializedName("place_name") val placeName: String,
    @SerializedName("address_name") val addressName: String,
    @SerializedName("road_address_name") val roadAddressName: String,
    @SerializedName("x") val x: String,
    @SerializedName("y") val y: String,
    @SerializedName("distance") val distance: String,
    @SerializedName("category_group_name") val categoryGroupName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("place_url") val placeUrl: String
)