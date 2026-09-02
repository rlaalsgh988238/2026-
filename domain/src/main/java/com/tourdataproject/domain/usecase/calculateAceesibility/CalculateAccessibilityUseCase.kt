package com.tourdataproject.domain.usecase.calculateAceesibility

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.domain.repository.ToiletRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CalculateAccessibilityUseCase @Inject constructor(
    private val toiletRepository: ToiletRepository,
    private val calculator: AccessibilityCalculator
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        searchRadius: Float = 1500f//TODO: 화장실 거리값 생각
    ): AccessibilityInfo {

        // 🌟 핵심 수정: Loading 상태는 무시하고 Success나 Error가 나올 때까지 기다림
        val resource = toiletRepository.getToiletsByDistance(searchRadius, latitude, longitude)
            .filter { it !is DataResource.Loading }
            .first()

        val allToilets = when (resource) {
            is DataResource.Success -> resource.data
            is DataResource.Error -> throw RuntimeException(resource.throwable.message ?: "화장실 데이터를 불러오지 못했습니다.")
            is DataResource.Loading -> emptyList() // 이제 여기로 들어올 일은 없습니다.
        }




        //거리 계산 후 가까운 순 정렬
        val sortedToilets = allToilets.map { toilet ->
            val time = calculator.calculateWalkingTime(latitude, longitude, toilet.latitude, toilet.longitude)
            Pair(toilet, time)
        }.sortedBy { it.second }

        val planA = sortedToilets.getOrNull(0)
        val planB = sortedToilets.getOrNull(1)

        if (allToilets.isEmpty() || planA == null) {
            return AccessibilityInfo(
                status = AccessibilityStatus.BAD, // 또는 BAD에 해당하는 상태값
                safetyScore = 0,
                planAToiletId = null,
                planBToiletId = null
            )
        }

        val timeAB = if (planB != null) {
            calculator.calculateWalkingTime(
                planA.first.latitude, planA.first.longitude,
                planB.first.latitude, planB.first.longitude
            )
        } else null

        val (finalScore, finalStatus) = calculator.calculateFinalScore(
            timeA = planA.second,
            count = allToilets.size,
            timeB = timeAB
        )

        return AccessibilityInfo(
            status = finalStatus,
            safetyScore = finalScore,
            planAToiletId = planA.first.id.toString(),
            planBToiletId = planB?.first?.id?.toString()
        )
    }
}