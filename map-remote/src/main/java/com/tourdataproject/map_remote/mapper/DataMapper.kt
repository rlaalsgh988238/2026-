package com.tourdataproject.map_remote.mapper

import com.google.gson.Gson
import com.tourdataproject.map_data.model.KakaoMapDataModel
import com.tourdataproject.map_remote.response.DocumentDto
import com.tourdataproject.map_remote.response.MapItemRemoteModel
import com.tourdataproject.map_remote.response.SearchResponseDto

// DocumentDto를 MapItemRemoteModel로 변환하는 역할을 전담합니다.
fun DocumentDto.toDataModel(): MapItemRemoteModel {
    return MapItemRemoteModel(
        id = this.id,
        placeName = this.placeName,
        addressName = this.addressName,
        roadAddressName = this.roadAddressName,
        // DTO의 x(경도), y(위도) String 값을 Double로 안전하게 변환
        longitude = this.x.toDoubleOrNull() ?: 0.0,
        latitude = this.y.toDoubleOrNull() ?: 0.0,
        // 거리는 계산하기 편하게 Int로 변환
        distance = this.distance.toIntOrNull() ?: 0,
        categoryGroupName = this.categoryGroupName,
        phone = this.phone,
        placeUrl = this.placeUrl
    )
}

// 2. ByteArray를 받아 파싱한 뒤, Data 계층의 Model로 변환해 주는 Remote 전용 매퍼
fun ByteArray.toDataModelList(): List<KakaoMapDataModel> {
    // ByteArray를 다시 JSON String으로 복원
    val jsonString = String(this, Charsets.UTF_8)

    // JSON String을 SearchResponseDto로 역직렬화
    val responseDto = Gson().fromJson(jsonString, SearchResponseDto::class.java)

    // 풍부해진 DTO 데이터를 KakaoMapDataModel에 빠짐없이 맵핑
    return responseDto.documents.map { document ->
        KakaoMapDataModel(
            id = document.id,
            placeName = document.placeName,
            addressName = document.addressName,
            roadAddressName = document.roadAddressName,
            longitude = document.x.toDoubleOrNull() ?: 0.0,
            latitude = document.y.toDoubleOrNull() ?: 0.0,
            distance = document.distance.toIntOrNull() ?: 0,
            categoryGroupName = document.categoryGroupName,
            phone = document.phone,
            placeUrl = document.placeUrl
        )
    }
}