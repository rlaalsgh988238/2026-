package com.tourdataproject.tourdata_remote.model.dto

import com.google.gson.annotations.SerializedName

data class LocationItem(
    @SerializedName("contentid") val contentId: String, //[cite: 1]
    @SerializedName("title") val title: String, //[cite: 1]
    @SerializedName("dist") val dist: String // 중심 좌표로부터 거리[cite: 1]
)

