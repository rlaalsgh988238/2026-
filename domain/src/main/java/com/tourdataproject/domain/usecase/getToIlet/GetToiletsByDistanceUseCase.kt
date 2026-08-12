package com.tourdataproject.domain.usecase.getToIlet

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
import com.tourdataproject.domain.model.Toilet
import com.tourdataproject.domain.repository.ToiletRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 미터단위로 Distance 설정
 */
class GetToiletsByDistanceUseCase @Inject constructor(
    private val toiletRepository: ToiletRepository
) {
    operator fun invoke(
        distance: Float,
        longitude: Double,
        latitude: Double
    ) = toiletRepository.getToiletsByDistance(distance, longitude, latitude)
        .onSuccess {

        }
        .onError {

        }
}