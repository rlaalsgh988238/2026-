package com.tourdataproject.map_data.repositoryImpl

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.mapListDataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.repository.KakaoMapRepository
import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_data.mapper.toDomainModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class KakaoMapRepositoryImpl @Inject constructor(
    private val remoteDataSource: KakaoMapRemoteDataSource
) : KakaoMapRepository {

    override fun getNearbyPlaces(
        query: String,
        longitude: Double,
        latitude: Double,
        radius: Int
    ): Flow<DataResource<List<KakaoMapItem>>> {

        // 1. RemoteDataSource에서 Flow<DataResource<List<KakaoMapDataModel>>>를 가져옴
        val remoteFlow = remoteDataSource.searchAddress(query = query, page = 1)

        // 2. 아까 만들어 둔 mapListDataResource를 써서 Data 모델을 Domain 모델(KakaoMapItem)로 싹 변환!
        return remoteFlow.mapListDataResource { dataModel ->
            dataModel.toDomainModel()
        }
    }
}