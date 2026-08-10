package com.tourdataproject.map_data.datasource

import com.braveberry.data_resource.DataResource
import com.tourdataproject.map_data.model.KakaoMapDataModel
import kotlinx.coroutines.flow.Flow

interface KakaoMapRemoteDataSource {
     fun getNearbyPlaces(
          query: String,
          longitude: Double? = null,
          latitude: Double? = null,
          radius: Int? = null,
          page: Int
     ): Flow<DataResource<List<KakaoMapDataModel>>>
}