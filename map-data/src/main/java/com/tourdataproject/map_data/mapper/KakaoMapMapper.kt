package com.tourdataproject.map_data.mapper

import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.map_data.model.KakaoMapDataModel

// Data 계층의 Model Mapper는 오직 Data Model을 Domain Model로
fun KakaoMapDataModel.toDomainModel(): KakaoMapItem {
    return KakaoMapItem(
        address = this.address,
        longitude = this.longitude,
        latitude = this.latitude
    )
}
