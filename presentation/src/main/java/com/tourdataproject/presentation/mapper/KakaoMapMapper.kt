package com.tourdataproject.presentation.mapper

import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.presentation.model.KakaoMapUiModel
import java.util.Locale

fun KakaoMapItem.toUiModel(): KakaoMapUiModel {
    val displayAddress = this.roadAddressName.ifBlank { this.addressName }

    // 2. 미터(m) 단위 거리를 보기 좋게 변환 (예: 1500 -> 1.5km, 500 -> 500m)
    val distanceText = if (this.distance >= 1000) {
        "%.1fkm".format(Locale.KOREA, this.distance / 1000.0)
    } else {
        "${this.distance}m"
    }

    return KakaoMapUiModel(
        id = this.id,
        placeName = this.placeName,
        address = displayAddress,
        distanceText = distanceText,
        x = this.x,
        y = this.y,
        category = this.category,
        phone = this.phone
    )
}