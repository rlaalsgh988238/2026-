package com.tourdataproject.domain.usecase.plan

import com.tourdataproject.domain.repository.MapRepository
import javax.inject.Inject

class GetRegionByKeywordUseCase @Inject constructor(
    private val mapRepository: MapRepository
) {
    operator fun invoke(keyword: String) =
        mapRepository.getRegionByKeyword(keyword)
}