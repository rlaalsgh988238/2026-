package com.tourdataproject.map_remote

import com.tourdataproject.map_remote.response.DocumentDto
import com.tourdataproject.map_remote.response.MapItemRemoteModel

// DocumentDto를 MapItemRemoteModel로 변환하는 역할을 전담합니다.
fun DocumentDto.toDataModel(): MapItemRemoteModel {
    return MapItemRemoteModel(
        addressName = this.addressName,
        // String으로 넘어온 좌표를 Double로 안전하게 변환
        longitude = this.longitude.toDoubleOrNull() ?: 0.0,
        latitude = this.latitude.toDoubleOrNull() ?: 0.0
    )
}