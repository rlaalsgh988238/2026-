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

        // 🌟 3. combine을 통해 두 Flow의 결과를 하나로 묶습니다.
        return combine(globalSearchFlow, localSearchFlow) { globalResource, localResource ->

            // 로딩 상태 처리: 둘 중 하나라도 로딩 중이면 UI에 로딩 스피너를 띄움
            if (globalResource is DataResource.Loading || localResource is DataResource.Loading) {
                return@combine DataResource.Loading()
            }

            // 에러 상태 처리: 둘 다 완전히 실패했을 때만 에러 반환
            // (둘 중 하나라도 성공했다면, 성공한 데이터라도 보여주는 것이 사용자 경험에 좋습니다)
            if (globalResource is DataResource.Error && localResource is DataResource.Error) {
                val errorMsg = globalResource.throwable ?: localResource.throwable
                return@combine DataResource.Error(errorMsg ?: Exception("검색 결과를 불러오지 못했습니다."))
            }

            // 각 검색 결과에서 데이터를 안전하게 추출 (실패했으면 빈 리스트 처리)
            val globalData =
                if (globalResource is DataResource.Success) globalResource.data else emptyList()
            val localData =
                if (localResource is DataResource.Success) localResource.data else emptyList()

            // 🌟 4. 데이터 병합 및 중복 제거
            // 전국 검색 결과(광주광역시)를 리스트 상단에 두고, 이어서 주변 검색 결과(광주식당)를 붙입니다.
            // distinctBy를 사용해 두 검색 결과에서 겹치는 장소가 있다면 하나로 합쳐줍니다.
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
