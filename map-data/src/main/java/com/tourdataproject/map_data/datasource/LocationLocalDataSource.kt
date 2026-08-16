package com.tourdataproject.map_data.datasource

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Location
import com.tourdataproject.map_data.model.LocationDataModel
import kotlinx.coroutines.flow.Flow

interface LocationLocalDataSource {
    // 로컬 DB에서 저장된 좌표를 가져오는 함수 (비동기)
     suspend fun getUserLocation(): Pair<Double, Double>? // (경도, 위도)
    fun getUserLocationFlow(): Flow<DataResource<LocationDataModel>>
}