package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Region
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getNearbyPlaces(
        query: String,
        longitude: Double? = null,
        latitude: Double? = null,
        radius: Int? = null,
        page: Int = 1
    ): Flow<DataResource<List<KakaoMapItem>>>

    fun getUserLocation(): Flow<DataResource<Location>>

    fun getPopularCity(): Flow<DataResource<List<Region>>>
}