package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.KakaoMapItem
import kotlinx.coroutines.flow.Flow

interface KakaoMapRepository {
     fun getNearbyPlaces(
        query: String,
        longitude: Double? = null,
        latitude: Double? = null,
        radius: Int? = null,
        page: Int = 1
    ): Flow<DataResource<List<KakaoMapItem>>>
}