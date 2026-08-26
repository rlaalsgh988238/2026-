package com.tourdataproject.domain.usecase.calculateAceesibility

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.repository.ToiletRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CalculateAccessibilityUseCase @Inject constructor(
    private val toiletRepository: ToiletRepository,
    private val calculator: AccessibilityCalculator
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        searchRadius: Float = 1500f
    ): AccessibilityInfo {

        val resource = toiletRepository.getToiletsByDistance(searchRadius, latitude, longitude).first()
        val allToilets = (resource as? DataResource.Success)?.data ?: emptyList()

        //거리 계산 후 가까운 순 정렬
        val sortedToilets = allToilets.map { toilet ->
            val time = calculator.calculateWalkingTime(latitude, longitude, toilet.latitude, toilet.longitude)
            Pair(toilet, time)
        }.sortedBy { it.second }

        val planA = sortedToilets.getOrNull(0)
        val planB = sortedToilets.getOrNull(1)


        val timeAB = if (planA != null && planB != null) {
            calculator.calculateWalkingTime(
                planA.first.latitude, planA.first.longitude,
                planB.first.latitude, planB.first.longitude
            )
        } else null


        val (finalScore, finalStatus) = calculator.calculateFinalScore(
            timeA = planA?.second ?: 0,
            count = allToilets.size,
            timeB = timeAB
        )


        return AccessibilityInfo(
            status = finalStatus,
            safetyScore = finalScore,
            planAToiletId = planA?.first?.id?.toString(),
            planBToiletId = planB?.first?.id?.toString()
        )
    }
}