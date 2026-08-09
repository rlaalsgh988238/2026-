package com.tourdataproject.domain.repository

import com.tourdataproject.domain.model.Toilet
import kotlinx.coroutines.flow.Flow
import com.braveberry.data_resource.DataResource

interface ToiletRepository {
    fun getToiletById(id: String): Flow<DataResource<Toilet>>
    fun getAllToilets(): Flow<DataResource<List<Toilet>>>
    fun getToiletsByDistance(Distance: Float, latitude: Double, longitude: Double): Flow<DataResource<List<Toilet>>>
    fun getToiletsByName(name: String): Flow<DataResource<List<Toilet>>>
}