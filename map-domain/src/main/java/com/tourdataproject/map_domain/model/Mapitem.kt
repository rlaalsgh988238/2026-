package com.tourdataproject.map_domain.model

data class MapItem(
    val address: String,     // 주소
    val longitude: Double,   // x 좌표 (지도 SDK에 바로 넘기기 편한 Double형)
    val latitude: Double     // y 좌표 (지도 SDK에 바로 넘기기 편한 Double형)
)