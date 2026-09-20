package com.tourdataproject.map_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.mapDataResource
import com.braveberry.data_resource.mapListDataResource
import com.tourdataproject.domain.model.Region
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.repository.MapRepository
import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import com.tourdataproject.map_data.datasource.RegionLocalDataSource
import com.tourdataproject.map_data.mapper.toDomain
import com.tourdataproject.map_data.mapper.toDomainModel
import com.tourdataproject.map_data.uitlity.calculateLocationParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val remoteDataSource: KakaoMapRemoteDataSource,
    private val locationLocalDataSource: LocationLocalDataSource,
    private val regionLocalDataSource: RegionLocalDataSource
) : MapRepository {

    override suspend fun getNearbyPlaces(
        query: String,
        longitude: Double?,
        latitude: Double?,
        radius: Int?,
        page: Int
    ): List<KakaoMapItem> {

        val params = locationLocalDataSource.calculateLocationParams(longitude, latitude, radius)

        val dataModels = remoteDataSource.getNearbyPlaces(
            query = query,
            longitude = params.lng,
            latitude = params.lat,
            radius = params.radius,
            page = page
        )

        return dataModels.map { it.toDomainModel() }
    }
    override fun getUserLocation(): Flow<DataResource<Location>> =
        locationLocalDataSource.getUserLocationFlow().mapDataResource { it.toDomain() }

    override fun getPopularCity(): Flow<DataResource<List<Region>>> = flow{
        emit(DataResource.Loading())
        val result = regionLocalDataSource.getPopularRegion()
        emit(DataResource.Success(result.toDomain()))
    }.catch { e ->
        emit(DataResource.Error(e))
    }

    override fun getRegionPosition(regionName: String): Flow<DataResource<Location>> =
        remoteDataSource.getQueryPosition(regionName).mapDataResource {
            it.toDomain()
        }

    override fun getRegionByKeyword(keyword: String): Flow<DataResource<List<Region>>> = flow {
        emit(DataResource.loading())
        val result = regionLocalDataSource.getRegionByKeyword(keyword)
        emit(DataResource.Success(result.toDomain()))
    }.catch { e ->
        emit(DataResource.Error(e))
    }
}