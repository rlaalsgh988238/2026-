package com.tourdataproject.map_remote.datasource

interface MapRemoteDataSource {
    suspend fun searchAddress(query: String, page: Int): Result<ByteArray>
}