package com.tourdataproject.presentation.testing

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.KakaoMapItem
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.model.Region
import com.tourdataproject.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** 실제 UseCase는 그대로 사용하고 외부 데이터 경계만 대체합니다. */
class FakeMapRepository : MapRepository {
    val searchedKeywords = mutableListOf<String>()
    val requestedPositions = mutableListOf<String>()

    var popularCities: Flow<DataResource<List<Region>>> =
        flowOf(DataResource.Success(emptyList()))

    var search: (String) -> Flow<DataResource<List<Region>>> = {
        error("이 테스트에서 지역 검색 응답을 설정해야 합니다.")
    }

    var position: (String) -> Flow<DataResource<Location>> = {
        error("이 테스트에서 좌표 응답을 설정해야 합니다.")
    }

    override fun getPopularCity(): Flow<DataResource<List<Region>>> = popularCities

    override fun getRegionByKeyword(keyword: String): Flow<DataResource<List<Region>>> {
        searchedKeywords += keyword
        return search(keyword)
    }

    override fun getRegionPosition(regionName: String): Flow<DataResource<Location>> {
        requestedPositions += regionName
        return position(regionName)
    }

    override fun getUserLocation(): Flow<DataResource<Location>> =
        error("RegionSelection 테스트는 현재 위치를 요청하지 않아야 합니다.")

    override suspend fun getNearbyPlaces(
        query: String,
        longitude: Double?,
        latitude: Double?,
        radius: Int?,
        page: Int
    ): List<KakaoMapItem> =
        error("RegionSelection 테스트는 주변 장소를 요청하지 않아야 합니다.")
}
