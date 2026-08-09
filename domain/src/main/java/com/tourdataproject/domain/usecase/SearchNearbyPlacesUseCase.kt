package com.tourdataproject.domain.usecase

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.repository.KakaoMapRepository
import jdk.jfr.internal.OldObjectSample.emit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchNearbyPlacesUseCase @Inject constructor(
    private val mapRepository: KakaoMapRepository
) {
    // 1. suspend 키워드 제거 완료!
    operator fun invoke(
        query: String,
        longitude: Double,
        latitude: Double,
        radius: Int = 2000
    ): Flow<DataResource<List<KakaoMapItem>>> {

        // 2. 검색어가 비어있을 때 DataResource.error를 담은 Flow 방출
        if (query.isBlank()) {
            return flow {
                emit(DataResource.error(IllegalArgumentException("검색어를 입력해주세요.")))
            }
        }

        // 3. Repository에 데이터 요청
        return mapRepository.getNearbyPlaces(query, longitude, latitude, radius)
    }
}