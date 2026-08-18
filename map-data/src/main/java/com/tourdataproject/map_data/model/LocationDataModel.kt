package com.tourdataproject.map_data.model

import com.tourdataproject.domain.model.Location
import com.tourdataproject.map_data.mapper.DataMapper

data class LocationDataModel(
    val latitude: Double,
    val longitude: Double
): DataMapper<Location> {
    override fun toDomain(): Location{
        return Location(
            latitude = this.latitude,
            longitude = this.longitude
        )
    }
}
