package com.tourdataproject.domain.usecase

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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
    ): Flow<DataResource<List<KakaoMapItem>>> {

        if (query.isBlank()) {
            return flowOf(DataResource.Error(IllegalArgumentException("검색어를 입력해주세요.")))
        }

        // 자동완성: 주변 검색만 (빠름)
        if (!includeGlobalSearch) {
            return mapRepository.getNearbyPlaces(
                query = query,
                longitude = longitude,
                latitude = latitude,
                radius = radius,
                page = page
            )
        }

        // 일반 검색: 전국 + 주변 (느림)
        val globalSearchFlow = mapRepository.getNearbyPlaces(
            query = query,
            longitude = null,
            latitude = null,
            radius = null,
            page = 1
        )

        val localSearchFlow = mapRepository.getNearbyPlaces(
            query = query,
            longitude = longitude,
            latitude = latitude,
            radius = radius,
            page = page
        )

        return combine(globalSearchFlow, localSearchFlow) { globalResource, localResource ->

            if (globalResource is DataResource.Loading || localResource is DataResource.Loading) {
                return@combine DataResource.Loading()
            }

            if (globalResource is DataResource.Error && localResource is DataResource.Error) {
                val errorMsg = globalResource.throwable ?: localResource.throwable
                return@combine DataResource.Error(errorMsg ?: Exception("검색 결과를 불러오지 못했습니다."))
            }

            val globalData =
                if (globalResource is DataResource.Success) globalResource.data else emptyList()
            val localData =
                if (localResource is DataResource.Success) localResource.data else emptyList()

            val combinedList = (localData + globalData).distinctBy { it.id }

            DataResource.Success(combinedList)
        }
    }
}
