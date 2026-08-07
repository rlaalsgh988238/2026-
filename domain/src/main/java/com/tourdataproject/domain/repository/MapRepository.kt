package com.tourdataproject.domain.repository

import com.tourdataproject.domain.model.MapItem

interface MapRepository {
    suspend fun getNearbyPlaces(
        query: String,
        longitude: Double,
        latitude: Double,
        radius: Int
    ): Result<List<MapItem>>
}