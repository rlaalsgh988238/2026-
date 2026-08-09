package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.KakaoMapItem
import kotlinx.coroutines.flow.Flow

interface KakaoMapRepository {
    // Result 대신 Flow<DataResource<T>>를 반환하도록 변경!
    fun getNearbyPlaces(
        query: String,
        longitude: Double,
        latitude: Double,
        radius: Int
    ): Flow<DataResource<List<KakaoMapItem>>>
}