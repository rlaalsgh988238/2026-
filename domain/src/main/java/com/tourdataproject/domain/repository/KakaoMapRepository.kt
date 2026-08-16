package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface KakaoMapRepository {
    fun getNearbyPlaces(
        query: String,
        // 위치값 없을 때 위해서
        longitude: Double? = null,
        latitude: Double? = null,
        radius: Int? = null,
        page: Int = 1
    ): Flow<DataResource<List<KakaoMapItem>>>

    fun getUserLocation(): Flow<DataResource<Location>>
}