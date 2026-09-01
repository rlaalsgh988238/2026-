package com.tourdataproject.domain.usecase.plan

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRegionPositionUseCase @Inject constructor(
    private val mapRepository: MapRepository
) {
    operator fun invoke(regionName: String) : Flow<DataResource<Location>> =
        mapRepository.getRegionPosition(regionName)
}