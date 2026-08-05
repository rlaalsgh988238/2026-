package com.tourdataproject.map_domain.repository

import com.tourdataproject.map_domain.model.MapItem

interface MapRepository {
    suspend fun getNearbyPlaces(
        query: String,
        longitude: Double,
        latitude: Double,
        radius: Int
    ): Result<List<MapItem>>
}