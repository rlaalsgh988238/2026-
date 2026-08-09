package com.tourdataproject.map_remote.mapper

import com.google.gson.Gson
import com.tourdataproject.map_data.model.KakaoMapDataModel
import com.tourdataproject.map_remote.response.DocumentDto
import com.tourdataproject.map_remote.response.MapItemRemoteModel
import com.tourdataproject.map_remote.response.SearchResponseDto

// DocumentDto를 MapItemRemoteModel로 변환하는 역할을 전담합니다.
fun DocumentDto.toDataModel(): MapItemRemoteModel {
    return MapItemRemoteModel(
        addressName = this.addressName,
        // String으로 넘어온 좌표를 Double로 안전하게 변환
        longitude = this.longitude.toDoubleOrNull() ?: 0.0,
        latitude = this.latitude.toDoubleOrNull() ?: 0.0
    )
}

// ByteArray를 받아 파싱한 뒤, Data 계층의 Model로 변환해 주는 Remote 전용 매퍼
fun ByteArray.toDataModelList(): List<KakaoMapDataModel> {
    val jsonString = String(this, Charsets.UTF_8)
    val responseDto = Gson().fromJson(jsonString, SearchResponseDto::class.java)

    return responseDto.documents.map { document ->
        KakaoMapDataModel(
            address = document.addressName,
            longitude = document.longitude.toDoubleOrNull() ?: 0.0,
            latitude = document.latitude.toDoubleOrNull() ?: 0.0
        )
    }
}