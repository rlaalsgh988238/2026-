package com.tourdataproject.map_data.mapper

import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.map_data.model.KakaoMapDataModel

// Data 계층의 Model Mapper는 오직 Data Model을 Domain Model로
fun KakaoMapDataModel.toDomainModel(): KakaoMapItem {
    return KakaoMapItem(
        id = this.id,
        placeName = this.placeName,
        addressName = this.addressName,
        roadAddressName = this.roadAddressName,
        distance = this.distance,
        // DataModel의 longitude/latitude를 도메인의 x/y로 매핑
        x = this.longitude,
        y = this.latitude,

        category = this.categoryGroupName,
        phone = this.phone
    )
}