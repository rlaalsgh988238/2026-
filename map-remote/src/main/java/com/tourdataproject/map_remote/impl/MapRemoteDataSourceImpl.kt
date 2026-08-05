package com.tourdataproject.map_remote.impl

import com.tourdataproject.map_remote.MapApi
import com.tourdataproject.map_remote.datasource.MapRemoteDataSource
import javax.inject.Inject

class MapRemoteDataSourceImpl @Inject constructor(
    private val mapApi: MapApi // Retrofit API 인터페이스 주입
) : MapRemoteDataSource {

    override suspend fun searchAddress(query: String, page: Int): Result<ByteArray> {
        val response = mapApi.getSearch(query, page)

        return if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                // 성공한 경우 ByteArray를 Result.success로 감싸서 반환
                Result.success(body.toData())
            } else {
                Result.failure(IllegalStateException("Response body is null"))
            }
        } else {
            Result.failure(IllegalStateException("Network error: ${response.code()}"))
        }
    }
}