package com.tourdataproject.tourdata_remote.model.dto

import com.google.gson.annotations.SerializedName

data class DetailWithTourItem(
    @SerializedName("contentid") val contentId: String, //[cite: 1]
    @SerializedName("parking") val parking: String?, // 장애인 주차장[cite: 1]
    @SerializedName("route") val route: String?, // 접근로 (경사로 등)[cite: 1]
    @SerializedName("elevator") val elevator: String?, // 엘리베이터[cite: 1]
    @SerializedName("restroom") val restroom: String?, // 장애인 화장실[cite: 1]
    @SerializedName("wheelchair") val wheelchair: String?, // 휠체어 대여[cite: 1]
    @SerializedName("exit") val exit: String? // 출입통로[cite: 1]
)
