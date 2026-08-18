package com.tourdataproject.map_remote.model.dto

import com.google.gson.annotations.SerializedName

data class DocumentDto(
    val id: String,
    @SerializedName("place_name") val placeName: String,
    @SerializedName("address_name") val addressName: String, // 지번 주소
    @SerializedName("road_address_name") val roadAddressName: String, // 도로명 주소 (없을 수도 있음)
    val x: String,
    val y: String,
    val distance: String, // 중심 좌표까지의 거리 (단위: 미터)
    @SerializedName("category_group_name") val categoryGroupName: String, // 카테고리 (예: "음식점", "카페")
    val phone: String, // 전화번호
    @SerializedName("place_url") val placeUrl: String // 장소 상세페이지 URL
)