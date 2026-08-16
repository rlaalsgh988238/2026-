package com.braveberry.local.impl

import com.braveberry.local.provider.LocalLocationProvider
import com.braveberry.local.mapper.LocalLocationMapper
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import javax.inject.Inject

class LocationLocalDataSourceImpl @Inject constructor(
    private val locationProvider: LocalLocationProvider,
    private val locationMapper: LocalLocationMapper
) : LocationLocalDataSource {

    override suspend fun getUserLocation(): Pair<Double, Double>? {
        // 1. Provider를 통해 기기의 위치를 단일화된 ByteArray 포맷으로 가져옵니다.
        val rawData: ByteArray? = locationProvider.getCurrentLocationAsByteArray()

        // 2. 데이터가 없다면(GPS 꺼짐, 권한 없음 등) null을 반환하여 Repository가 전국 검색을 하도록 유도합니다.
        if (rawData == null) return null

        // 3. Mapper를 사용해 Repository가 원하는 Pair<Double, Double> 형태로 파싱하여 반환합니다.
        return locationMapper.mapFromByteArray(rawData)
    }
}