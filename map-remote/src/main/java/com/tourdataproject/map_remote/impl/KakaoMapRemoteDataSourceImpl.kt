package com.tourdataproject.map_remote.impl

import com.braveberry.data_resource.DataResource
import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_data.model.KakaoMapDataModel
import com.tourdataproject.map_data.model.LocationDataModel
import com.tourdataproject.map_remote.api.KakaoMapApi
import com.tourdataproject.map_remote.mapper.toDataModelList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
    ): Flow<DataResource<List<KakaoMapDataModel>>> =
        flow<DataResource<List<KakaoMapDataModel>>> {
            emit(DataResource.Loading())

            val response = kakaoMapApi.getSearch(
                query = query,
                longitude = longitude,
                latitude = latitude,
                radius = radius,
                page = page
            )

            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "장소 검색 요청 실패: HTTP ${response.code()}"
                )
            }

            val body = response.body()
                ?: throw IllegalStateException(
                    "장소 검색 응답 본문이 없습니다."
                )

            val dataModels = body.toData().toDataModelList()

            emit(DataResource.Success(dataModels))
        }.catch { e ->
            if (e is CancellationException) {
                throw e
            }

            emit(DataResource.Error(e))
        }

    override fun getQueryPosition(
        query: String
    ): Flow<DataResource<LocationDataModel>> =
        flow<DataResource<LocationDataModel>> {
            emit(DataResource.Loading())

            val response = kakaoMapApi.getRegionCoordinate(
                query = query,
                page = 1
            )

            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "지역 좌표 조회 요청 실패: HTTP ${response.code()}"
                )
            }

            val body = response.body()
                ?: throw IllegalStateException(
                    "지역 좌표 조회 응답 본문이 없습니다."
                )

            val document = body.documents.firstOrNull()
                ?: throw NoSuchElementException(
                    "해당 지역의 좌표 정보를 찾을 수 없습니다: $query"
                )

            // 카카오 좌표: x는 경도, y는 위도
            val latitude = document.y.toDoubleOrNull()
                ?: throw IllegalStateException(
                    "위도 값을 숫자로 변환할 수 없습니다."
                )

            val longitude = document.x.toDoubleOrNull()
                ?: throw IllegalStateException(
                    "경도 값을 숫자로 변환할 수 없습니다."
                )

            if (
                !latitude.isFinite() ||
                !longitude.isFinite() ||
                latitude !in -90.0..90.0 ||
                longitude !in -180.0..180.0
            ) {
                throw IllegalStateException(
                    "조회된 좌표가 유효한 범위를 벗어났습니다."
                )
            }

            emit(
                DataResource.Success(
                    LocationDataModel(
                        latitude = latitude,
                        longitude = longitude
                    )
                )
            )
        }.catch { e ->
            if (e is CancellationException) {
                throw e
            }

            emit(DataResource.Error(e))
        }
}
