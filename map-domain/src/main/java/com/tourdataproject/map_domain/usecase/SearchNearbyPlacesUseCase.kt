package com.tourdataproject.map_domain.usecase

import com.tourdataproject.map_domain.model.MapItem
import com.tourdataproject.map_domain.repository.MapRepository
import javax.inject.Inject

class SearchNearbyPlacesUseCase @Inject constructor(
    private val mapRepository: MapRepository
) {
    /**
     * @param query 검색어
     * @param longitude 중심점 x 좌표
     * @param latitude 중심점 y 좌표
     * @param radius 검색 반경 (기본값 2000m)
     */
    suspend operator fun invoke(
        query: String,
        longitude: Double,
        latitude: Double,
        radius: Int = 2000
    ): Result<List<MapItem>> {

        // 1. 도메인 로직 처리 (예: 빈 검색어 방어)
        if (query.isBlank()) {
            return Result.failure(IllegalArgumentException("검색어를 입력해주세요."))
        }

        // 2. Repository에 데이터 요청
        return mapRepository.getNearbyPlaces(query, longitude, latitude, radius)
    }
}