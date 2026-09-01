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
        emit(DataResource.Loading())
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

    override fun getQueryPosition(query: String): Flow<DataResource<LocationDataModel>> = flow {
        emit(DataResource.loading())
        try {
            // 1. 카카오 주소 검색 API 호출
            val response = kakaoMapApi.getRegionCoordinate(
                query = query,
                page = 1
            )

            if (response.isSuccessful) {
                val body = response.body()
                val firstDocument = body?.documents?.firstOrNull()

                if (firstDocument != null) {
                    // 2. 첫 번째 검색 결과에서 좌표를 추출하여 데이터 모델로 변환
                    // x는 경도(longitude), y는 위도(latitude)입니다.
                    val location = LocationDataModel(
                        latitude = firstDocument.y.toDouble(),
                        longitude = firstDocument.x.toDouble()
                    )
                    emit(DataResource.success(location))
                } else {
                    // 검색 결과가 없는 경우
                    emit(DataResource.error(NoSuchElementException("해당 지역의 좌표 정보를 찾을 수 없습니다.")))
                }
            } else {
                // 서버 에러 발생 시
                emit(DataResource.error(IllegalStateException("Network error: ${response.code()}")))
            }
        } catch (e: Exception) {
            // 네트워크 장애 등 예외 처리
            emit(DataResource.error(e))
        }
    }
}