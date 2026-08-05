package com.tourdataproject.map_remote

import com.tourdataproject.map_remote.response.SearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MapApi {
    @GET("v2/local/search/address")
    suspend fun getSearch(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Response<SearchResponseDto>
}