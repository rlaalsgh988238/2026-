package com.tourdataproject.domain.usecase.tourData

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.repository.TourRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTourDataToiletInfoUseCase @Inject constructor(
    private val tourRepository: TourRepository
) {
    operator fun invoke(contentId: String): Flow<DataResource<AccessibilityInfo>> {
        return tourRepository.getTourDataToiletInfo(contentId)
    }
}