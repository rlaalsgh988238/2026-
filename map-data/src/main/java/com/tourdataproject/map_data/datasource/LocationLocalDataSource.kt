package com.tourdataproject.map_data.datasource

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Location
import com.tourdataproject.map_data.model.LocationDataModel
import kotlinx.coroutines.flow.Flow

interface LocationLocalDataSource {
     suspend fun getUserLocation(): Pair<Double, Double>? // (경도, 위도)
     fun getUserLocationFlow(): Flow<DataResource<LocationDataModel>>
}