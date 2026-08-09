package com.tourdataproject.map_data.datasource

import com.braveberry.data_resource.DataResource
import com.tourdataproject.map_data.model.KakaoMapDataModel
import kotlinx.coroutines.flow.Flow

interface KakaoMapRemoteDataSource {
     // 끝부분의 KakaoMapItem을 KakaoMapDataModel로 변경!
     fun searchAddress(query: String, page: Int): Flow<DataResource<List<KakaoMapDataModel>>>
}