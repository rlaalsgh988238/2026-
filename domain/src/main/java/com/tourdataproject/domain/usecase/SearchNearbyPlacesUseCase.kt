package com.tourdataproject.domain.usecase

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.repository.MapRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchNearbyPlacesUseCase @Inject constructor(
    private val mapRepository: MapRepository
) {
    operator fun invoke(
        query: String,
        longitude: Double? = null,
        latitude: Double? = null,
        radius: Int? = null,
        page: Int = 1,
        includeGlobalSearch: Boolean = true
    ): Flow<DataResource<List<KakaoMapItem>>> = flow {
        if (query.isBlank()) {
            emit(DataResource.Error(IllegalArgumentException("검색어를 입력해주세요.")))
            return@flow
        }

        emit(DataResource.Loading())

        try {
            coroutineScope {
                if (!includeGlobalSearch) {
                    val localData = mapRepository.getNearbyPlaces(
                        query = query,
                        longitude = longitude,
                        latitude = latitude,
                        radius = radius,
                        page = page
                    )
                    emit(DataResource.Success(localData))
                } else {
                    val globalDeferred = async {
                        mapRepository.getNearbyPlaces(
                            query = query,
                            longitude = null,
                            latitude = null,
                            radius = null,
                            page = 1
                        )
                    }

                    val localDeferred = async {
                        mapRepository.getNearbyPlaces(
                            query = query,
                            longitude = longitude,
                            latitude = latitude,
                            radius = radius,
                            page = page
                        )
                    }

                    val globalData = globalDeferred.await()
                    val localData = localDeferred.await()
                    val combinedList = (localData + globalData).distinctBy { it.id }

                    emit(DataResource.Success(combinedList))
                }
            }
        } catch (e: Exception) {
            emit(DataResource.Error(e))
        }
    }
}
