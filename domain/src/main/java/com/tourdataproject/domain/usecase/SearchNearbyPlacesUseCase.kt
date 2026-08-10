package com.tourdataproject.domain.usecase

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.repository.KakaoMapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchNearbyPlacesUseCase @Inject constructor(
    private val mapRepository: KakaoMapRepository
) {
    operator fun invoke(
        query: String,
        // 🌟 전국 검색도 가능하게 하려면 Nullable(? = null)로 두는 게 좋습니다!
        longitude: Double? = null,
        latitude: Double? = null,
        radius: Int? = null,
        page: Int = 1 // 🌟 페이징을 위해 무조건 추가!
    ): Flow<DataResource<List<KakaoMapItem>>> {

        if (query.isBlank()) {
            return flow {
                emit(DataResource.error(IllegalArgumentException("검색어를 입력해주세요.")))
            }
        }

        return mapRepository.getNearbyPlaces(query, longitude, latitude, radius, page)
    }
}