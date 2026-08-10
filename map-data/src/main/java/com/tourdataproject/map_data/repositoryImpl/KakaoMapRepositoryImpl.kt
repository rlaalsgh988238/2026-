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
        longitude: Double?,
        latitude: Double?,
        radius: Int?,
        page: Int
    ): Flow<DataResource<List<KakaoMapItem>>> {

        val remoteFlow = remoteDataSource.getNearbyPlaces(
            query = query,
            longitude = longitude,
            latitude = latitude,
            radius = radius,
            page = page
        )
        return remoteFlow.mapListDataResource { dataModel ->
            dataModel.toDomainModel()
        }
    }
}