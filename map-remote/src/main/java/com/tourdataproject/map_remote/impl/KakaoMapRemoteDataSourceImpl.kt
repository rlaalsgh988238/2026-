package com.tourdataproject.map_remote.impl

import com.braveberry.data_resource.DataResource
import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_data.model.KakaoMapDataModel
import com.tourdataproject.map_remote.api.KakaoMapApi
import com.tourdataproject.map_remote.mapper.toDataModelList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class KakaoMapRemoteDataSourceImpl @Inject constructor(
    private val kakaoMapApi: KakaoMapApi
) : KakaoMapRemoteDataSource {

    override fun getNearbyPlaces(
        query: String,
        longitude: Double?,
        latitude: Double?,
        radius: Int?,
        page: Int
    ): Flow<DataResource<List<KakaoMapDataModel>>> = flow {
        try {
            val response = kakaoMapApi.getSearch(
                query = query,
                longitude = longitude,
                latitude = latitude,
                radius = radius,
                page = page
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val dataModels = body.toData().toDataModelList()
                    emit(DataResource.success(dataModels))
                } else {
                    emit(DataResource.error(IllegalStateException("Response body is null")))
                }
            } else {
                emit(DataResource.error(IllegalStateException("Network error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(DataResource.error(e))
        }
    }
}