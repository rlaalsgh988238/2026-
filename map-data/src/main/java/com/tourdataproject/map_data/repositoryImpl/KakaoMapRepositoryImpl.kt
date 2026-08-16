package com.tourdataproject.map_data.repositoryImpl

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.mapListDataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.repository.KakaoMapRepository
import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import com.tourdataproject.map_data.mapper.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class KakaoMapRepositoryImpl @Inject constructor(
    private val remoteDataSource: KakaoMapRemoteDataSource,
    private val locationLocalDataSource: LocationLocalDataSource
) : KakaoMapRepository {

    override fun getNearbyPlaces(
        query: String,
        longitude: Double?,
        latitude: Double?,
        radius: Int?,
        page: Int
    ): Flow<DataResource<List<KakaoMapItem>>> = flow {

        var targetLng = longitude
        var targetLat = latitude

        if (targetLng == null || targetLat == null) {
            val localLocation = locationLocalDataSource.getUserLocation()
            targetLng = localLocation?.first
            targetLat = localLocation?.second
        }

        val targetRadius = if (targetLng != null && targetLat != null) radius ?: 2000 else null

        remoteDataSource.getNearbyPlaces(
            query = query,
            longitude = targetLng,
            latitude = targetLat,
            radius = targetRadius,
            page = page
        ).mapListDataResource { dataModel ->
            dataModel.toDomainModel()
        }.collect { resource ->
            emit(resource)
        }

    }.catch { e ->
        emit(DataResource.error(e))
    }

    override fun getUserLocation(): Flow<DataResource<Location>> =
        locationLocalDataSource.getUserLocationFlow()
}