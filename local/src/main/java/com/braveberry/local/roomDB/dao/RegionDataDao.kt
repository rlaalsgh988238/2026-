package com.braveberry.local.roomDB.dao

import androidx.room.Dao
import androidx.room.Query
import com.braveberry.local.model.region.RegionDataLocalModel
import com.braveberry.local.roomDB.RoomConstant

@Dao
internal interface RegionDataDao : BaseDao<RegionDataLocalModel> {

    @Query("SELECT * FROM ${RoomConstant.Table.REGION} LIMIT 1")
    suspend fun getAnyRegion(): RegionDataLocalModel?

    // 🌟 1. 인기 도시 목록 (로더에서 정확히 12개만 세팅했으므로 그대로 가져옵니다)
    @Query("""
        SELECT * FROM ${RoomConstant.Table.REGION} 
        WHERE isPopular = 1 
        ORDER BY province, city
    """)
    suspend fun getPopularRegions(): List<RegionDataLocalModel>

    // 🌟 2. 실시간 검색 (town IS NULL 조건을 주어 읍/면/동을 원천 차단)
    @Query("""
        SELECT * FROM ${RoomConstant.Table.REGION} 
        WHERE town IS NULL 
          AND (province LIKE '%' || :keyword || '%' OR city LIKE '%' || :keyword || '%')
        LIMIT 50
    """)
    suspend fun searchRegions(keyword: String): List<RegionDataLocalModel>

    @Query("UPDATE ${RoomConstant.Table.REGION} SET isPopular = :isPopular WHERE code = :code")
    suspend fun updatePopularityByCode(code: String, isPopular: Boolean)

    @Query("""
    UPDATE ${RoomConstant.Table.REGION} 
    SET isPopular = :isPopular 
    WHERE town IS NULL 
      AND (city = :cityName OR province = :cityName)
""")
    suspend fun updatePopularityByName(cityName: String, isPopular: Boolean)

}
