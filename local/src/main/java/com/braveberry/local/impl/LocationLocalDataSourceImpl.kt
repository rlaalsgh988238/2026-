package com.braveberry.local.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.mapDataResource
import com.braveberry.local.provider.LocalLocationProvider
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import com.tourdataproject.map_data.model.LocationDataModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

internal class LocationLocalDataSourceImpl @Inject constructor(
    private val locationProvider: LocalLocationProvider,
) : LocationLocalDataSource {

    override suspend fun getUserLocation(): Pair<Double, Double>? {

        val resource =
            locationProvider.provideUserLocation()
                .firstOrNull { it !is DataResource.Loading }

        // 2. 결과가 Success이고 데이터가 존재하면 Pair로 묶어서 반환
        return if (resource is DataResource.Success) {
            val model = resource.data
            Pair(model.latitude, model.longitude)
        } else {
            null
        }
    }

    override fun getUserLocationFlow(): Flow<DataResource<LocationDataModel>> {
        return locationProvider.provideUserLocation().mapDataResource{
            it.toData()
        }
    }
}