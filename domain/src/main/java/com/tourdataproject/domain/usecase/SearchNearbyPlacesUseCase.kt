package com.tourdataproject.domain.usecase

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
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
        page: Int = 1
    ): Flow<DataResource<List<KakaoMapItem>>> {

        if (query.isBlank()) {
            return flowOf(DataResource.Error(IllegalArgumentException("검색어를 입력해주세요.")))
        }

        // 1. 전국 단위 검색 (유명 랜드마크 우선순위 확보)
        val globalSearchFlow = mapRepository.getNearbyPlaces(
            query = query,
            longitude = null,
            latitude = null,
            radius = null,
            page = 1
        )

        // 2. 주변 검색 (현재 좌표 및 반경 기준 검색)
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
            //에러 어케 표시할지

            if (globalResource is DataResource.Error && localResource is DataResource.Error) {
                val errorMsg = globalResource.throwable ?: localResource.throwable
                return@combine DataResource.Error(errorMsg ?: Exception("검색 결과를 불러오지 못했습니다."))
            }

            val globalData =
                if (globalResource is DataResource.Success) globalResource.data else emptyList()
            val localData =
                if (localResource is DataResource.Success) localResource.data else emptyList()

            val combinedList =
                (localData + globalData).distinctBy { it.id } // KakaoMapItem에 고유 id 필드가 있다고 가정

            DataResource.Success(combinedList)
        }
    }
}


//        if (query.isBlank()) {
//            flowOf(DataResource.error(IllegalArgumentException("검색어를 입력해주세요.")))
//        } else {
//            mapRepository.getNearbyPlaces(query, longitude, latitude, radius, page)
//                .onSuccess { data ->
//
//                }
//                .onError { throwable ->
//                    //TODO: 에러 찍기? 혹은 어케하지
//                }
//        }
