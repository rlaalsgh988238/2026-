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
    operator fun invoke(lat: Double, lng: Double, radius: Int = 50): Flow<DataResource<String>> = flow {
        emit(DataResource.Loading())

        try {

            val locationResult = tourRepository.getLocationBasedList(lat, lng, radius)
                .first { it !is DataResource.Loading }

            when (locationResult) {
                is DataResource.Success -> {
                    if (locationResult.data.isNotEmpty()) {
                        val contentId = locationResult.data.first().contentId
                        emit(DataResource.Success(contentId))
                    } else {
                        emit(DataResource.Error(Exception("반경 내에 관광지 정보가 없습니다.")))
                    }
                }
                is DataResource.Error -> {
                    emit(DataResource.Error(locationResult.throwable))
                }
                else -> {}
            }
        } catch (e: Exception) {
            emit(DataResource.Error(e))
        }
    }
}