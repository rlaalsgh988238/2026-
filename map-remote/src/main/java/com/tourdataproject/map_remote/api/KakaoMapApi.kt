package com.tourdataproject.map_remote.api

import com.tourdataproject.map_remote.model.dto.SearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoMapApi {
    @GET("v2/local/search/keyword.json")
    suspend fun getSearch(
        @Query("query") query: String,

        @Query("x") longitude: Double? = null,
        @Query("y") latitude: Double? = null,
        @Query("radius") radius: Int? = null,

        @Query("page") page: Int
    ): Response<SearchResponseDto>
}