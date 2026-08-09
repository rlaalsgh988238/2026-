package com.braveberry.toilet_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.toilet_data.localDB.ToiletDataSource
import com.tourdataproject.domain.model.Toilet
import com.tourdataproject.domain.repository.ToiletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ToiletRepositoryImpl @Inject constructor(
    private val toiletDataSource: ToiletDataSource
): ToiletRepository{
    override fun getToiletById(id: String): Flow<DataResource<Toilet>> {
        TODO("Not yet implemented")
    }

    override fun getAllToilets(): Flow<DataResource<List<Toilet>>> {
        TODO("Not yet implemented")
    }

    override fun getToiletsByDistance(
        Distance: Float,
        latitude: Double,
        longitude: Double
    ): Flow<DataResource<List<Toilet>>> {
        TODO("Not yet implemented")
    }

    override fun getToiletsByName(name: String): Flow<DataResource<List<Toilet>>> {
        TODO("Not yet implemented")
    }
}