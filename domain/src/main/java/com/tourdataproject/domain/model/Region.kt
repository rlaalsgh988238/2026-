package com.tourdataproject.domain.model

data class Region(
    val code: String,     // 법정동코드 (10자리)
    val province: String,             // 시도명 (예: 서울특별시, 전라남도)
    val city: String?,                // 시군구명 (예: 종로구, 여수시)
    val town: String?,                // 읍면동명 (예: 청운동, 돌산읍)
    val village: String?,             // 리명 (예: 군내리)
    val isPopular: Boolean = false    // 인기 도시 여부
)
