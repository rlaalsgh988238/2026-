package com.tourdataproject.map_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.mapDataResource
import com.braveberry.data_resource.mapListDataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.repository.MapRepository
import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import com.tourdataproject.map_data.mapper.toDomainModel
import com.tourdataproject.map_data.uitlity.calculateLocationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val remoteDataSource: KakaoMapRemoteDataSource,
    private val locationLocalDataSource: LocationLocalDataSource
) : MapRepository {

    override fun getNearbyPlaces(
        query: String,
        longitude: Double?,
        latitude: Double?,
        radius: Int?,
        page: Int
    ): Flow<DataResource<List<KakaoMapItem>>> = flow {

        val params = locationLocalDataSource.calculateLocationParams(longitude, latitude, radius)

        emitAll(
            remoteDataSource.getNearbyPlaces(
                query = query,
                longitude = params.lng,
                latitude = params.lat,
                radius = params.radius,
                page = page
            ).mapListDataResource { dataModel -> dataModel.toDomainModel() }
        )

    }.catch { e ->
        emit(DataResource.error(e))
    }

    override fun getUserLocation(): Flow<DataResource<Location>> =
        locationLocalDataSource.getUserLocationFlow().mapDataResource { it.toDomain() }
}