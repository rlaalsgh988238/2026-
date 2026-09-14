package com.tourdataproject.tourdata_remote.api


import com.tourdataproject.tourdata_remote.model.dto.DetailWithTourItem
import com.tourdataproject.tourdata_remote.model.dto.KtoApiResponse
import com.tourdataproject.tourdata_remote.model.dto.LocationItem
import retrofit2.http.GET
import retrofit2.http.Query

interface TourApiService {

    // 위치기반 관광정보 조회
    @GET("locationBasedList2")
    suspend fun getLocationBasedList(
        @Query("mapX") mapX: String, //[cite: 1]
        @Query("mapY") mapY: String, //[cite: 1]
        @Query("radius") radius: String, //[cite: 1]
        @Query("arrange") arrange: String = "E", // E=거리순 정렬[cite: 1]
        @Query("numOfRows") numOfRows: Int = 10, //[cite: 1]
        @Query("pageNo") pageNo: Int = 1 //[cite: 1]
    ): KtoApiResponse<LocationItem>

    // 무장애여행 조회 (contentId로 조회)
    @GET("detailWithTour2")
    suspend fun getDetailWithTour(
        @Query("contentId") contentId: String, //[cite: 1]
        @Query("numOfRows") numOfRows: Int = 10, //[cite: 1]
        @Query("pageNo") pageNo: Int = 1 //[cite: 1]
    ): KtoApiResponse<DetailWithTourItem>
}