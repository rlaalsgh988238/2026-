package com.tourdataproject.map_remote.impl

import com.braveberry.data_resource.DataResource
import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_data.model.KakaoMapDataModel
import com.tourdataproject.map_data.model.LocationDataModel
import com.tourdataproject.map_remote.api.KakaoMapApi
import com.tourdataproject.map_remote.mapper.toDataModelList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class KakaoMapRemoteDataSourceImpl @Inject constructor(
    private val kakaoMapApi: KakaoMapApi
) : KakaoMapRemoteDataSource {

    override suspend fun getNearbyPlaces(
        query: String,
        longitude: Double?,
        latitude: Double?,
        radius: Int?,
        page: Int
    ): List<KakaoMapDataModel> {

        // 1. API 호출 (suspend 함수이므로 코루틴 안에서 대기)
        val response = kakaoMapApi.getSearch(
            query = query,
            longitude = longitude,
            latitude = latitude,
            radius = radius,
            page = page
        )

        // 2. 성공 시 데이터 반환, 실패 시 예외(throw) 발생
        if (response.isSuccessful) {
            val body = response.body() ?: throw IllegalStateException("Response body is null")
            return body.toData().toDataModelList()
        } else {
            throw IllegalStateException("Network error: ${response.code()}")
        }
    }

    override fun getQueryPosition(query: String): Flow<DataResource<LocationDataModel>> = flow {
        emit(DataResource.loading())
        try {
            val response = kakaoMapApi.getRegionCoordinate(
                query = query,
                page = 1
            )

            if (response.isSuccessful) {
                val body = response.body()
                val firstDocument = body?.documents?.firstOrNull()

                if (firstDocument != null) {
                   val location = LocationDataModel(
                        latitude = firstDocument.y.toDouble(),
                        longitude = firstDocument.x.toDouble()
                    )
                    emit(DataResource.success(location))
                } else {
                    emit(DataResource.error(NoSuchElementException("해당 지역의 좌표 정보를 찾을 수 없습니다.")))
                }
            } else {
                emit(DataResource.error(IllegalStateException("Network error: ${response.code()}")))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(DataResource.error(e))
        }
    }
}