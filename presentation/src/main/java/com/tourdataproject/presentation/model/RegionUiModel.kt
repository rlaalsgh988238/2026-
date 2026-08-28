package com.tourdataproject.presentation.model

import com.tourdataproject.domain.model.Region

data class RegionUiModel(
    val code: String,     // 법정동코드 (10자리)
    val province: String,             // 시도명 (예: 서울특별시, 전라남도)
    val city: String? = null,                // 시군구명 (예: 종로구, 여수시)
    val town: String? = null,                // 읍면동명 (예: 청운동, 돌산읍)
    val village: String? = null,             // 리명 (예: 군내리)
    val isPopular: Boolean = false    // 인기 도시 여부
)

internal fun RegionUiModel.toDomain() =
    Region(
        code,
        province,
        city,
        town,
        village,
        isPopular
    )
