package com.tourdataproject.domain.usecase

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
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
    ): Flow<DataResource<List<KakaoMapItem>>> =

        if (query.isBlank()) {
            flowOf(DataResource.error(IllegalArgumentException("검색어를 입력해주세요.")))
        } else {
            mapRepository.getNearbyPlaces(query, longitude, latitude, radius, page)
                .onSuccess { data ->

                }
                .onError { throwable ->
                    //TODO: 에러 찍기? 혹은 어케하지
                }
        }
}