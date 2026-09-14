package com.tourdataproject.domain.usecase.tourData

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.repository.TourRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetContentIdUseCase @Inject constructor(
    private val tourRepository: TourRepository
) {
    operator fun invoke(lat: Double, lng: Double, radius: Int = 50): Flow<DataResource<String>> {
        return tourRepository.getContentId(lat, lng, radius)
    }
}