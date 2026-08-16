package com.braveberry.local.model

import com.braveberry.local.mapper.LocalMapper
import com.tourdataproject.map_data.model.LocationDataModel

data class LocationLocalModel(
    val latitude: Double,
    val longitude: Double
): LocalMapper<LocationDataModel> {
    override fun toData(): LocationDataModel {
        return LocationDataModel(
            latitude = this.latitude,
            longitude = this.longitude
        )
    }
}