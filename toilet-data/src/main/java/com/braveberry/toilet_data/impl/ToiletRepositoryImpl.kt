package com.braveberry.toilet_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.toilet_data.localDB.ToiletDataSource
import com.braveberry.toilet_data.toDomain
import com.braveberry.toilet_data.utiltiy.FilterCalculator
import com.tourdataproject.domain.model.Toilet
import com.tourdataproject.domain.repository.ToiletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

internal class ToiletRepositoryImpl @Inject constructor(
    private val toiletDataSource: ToiletDataSource,
    private val filterCalculator: FilterCalculator
): ToiletRepository{
    override fun getToiletById(id: String): Flow<DataResource<Toilet?>> =
        flow<DataResource<Toilet?>> {
            val result = toiletDataSource.getToiletData(id)?.toDomain()
            emit(DataResource.Success(result))
        }.catch { e ->
            emit(DataResource.error(e))
        }

    override fun getAllToilets(): Flow<DataResource<List<Toilet>>> {
        TODO("Not yet implemented")
    }

    override fun getToiletsByDistance(
        distance: Float,
        latitude: Double,
        longitude: Double
    ): Flow<DataResource<List<Toilet>>> = flow<DataResource<List<Toilet>>> {
        val resultList = toiletDataSource.getToiletDataInBox(distance, latitude, longitude)
            .filter {
                filterCalculator.isInCircle(
                    latitude, longitude, it.latitude, it.longitude, distance
                )
            }
            .map { it.toDomain() }

        emit(DataResource.Success(resultList))
    }.catch { e ->
        emit(DataResource.error(e))
    }


    override fun getToiletsByName(name: String): Flow<DataResource<List<Toilet>>> = flow<DataResource<List<Toilet>>> {
        val resultList = toiletDataSource.getSimilarNameToiletData(name).toDomain()

        emit(DataResource.Success(resultList))
    }.catch { e ->
        emit(DataResource.error(e))
    }
}