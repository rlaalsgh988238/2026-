package com.tourdataproject.domain.usecase.plan

import com.tourdataproject.domain.repository.MapRepository
import javax.inject.Inject

class GetRegionPositionUseCase @Inject constructor(
    val mapRepository: MapRepository
) {
    operator fun invoke(regionName: String) =
        mapRepository.getRegionPosition(regionName)
}