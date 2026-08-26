package com.tourdataproject.domain.usecase.plan

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Region
import com.tourdataproject.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPopularCitiesUseCase @Inject constructor(private val mapRepository: MapRepository) {
    operator fun invoke(): Flow<DataResource<List<Region>>> =
        mapRepository.getPopularCity()
}