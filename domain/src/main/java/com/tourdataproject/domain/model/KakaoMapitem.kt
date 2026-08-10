package com.tourdataproject.domain.model

data class KakaoMapItem(
    val id: String,
    val placeName: String,
    val addressName: String,
    val roadAddressName: String,
    val distance: Int,
    val x: Double,
    val y: Double,
    val category: String,        // 카테고리 ("음식점", "카페" 등)
    val phone: String            // 전화번호
)