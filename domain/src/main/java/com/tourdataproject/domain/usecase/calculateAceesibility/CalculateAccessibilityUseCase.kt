package com.tourdataproject.domain.usecase.calculateAceesibility

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.domain.repository.ToiletRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class CalculateAccessibilityUseCase @Inject constructor(
    private val toiletRepository: ToiletRepository
) {
    //TODO: 걸음속도로 했는데 , 추후 수정 필요
    private val WALKING_SPEED_PER_MINUTE = 67.0

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        searchRadius: Float = 1500f // 최대 20분(약 1.3km) 커버를 위해 1.5km 반경 검색
    ): AccessibilityInfo {

        val resource = toiletRepository.getToiletsByDistance(searchRadius, latitude, longitude).first()
        val allToilets = (resource as? DataResource.Success)?.data ?: emptyList()

        // 2. 현재 위치 기준 거리 및 소요 시간(분) 계산 후, 가장 가까운 순으로 정렬
        val toiletsWithTime = allToilets.map { toilet ->
            val distance = calculateDistance(latitude, longitude, toilet.latitude, toilet.longitude)
            val time = (distance / WALKING_SPEED_PER_MINUTE).toInt()
            Pair(toilet, time)
        }.sortedBy { it.second }

        val toiletCount = toiletsWithTime.size
        val planA = toiletsWithTime.getOrNull(0)
        val planB = toiletsWithTime.getOrNull(1)

        val scoreA = planA?.let { getScoreForTimeA(it.second) } ?: 0
        val scoreCount = getScoreForCount(toiletCount)

        // Plan A -> Plan B 소요 시간 계산
        val scoreB = if (planA != null && planB != null) {
            val distAB = calculateDistance(
                planA.first.latitude, planA.first.longitude,
                planB.first.latitude, planB.first.longitude
            )
            val timeAB = (distAB / WALKING_SPEED_PER_MINUTE).toInt()
            getScoreForTimeB(timeAB)
        } else {
            0
        }

        // 4. 가중치(60%, 20%, 20%) 반영 및 총점 계산
        val totalScore = (scoreA * 0.6) + (scoreCount * 0.2) + (scoreB * 0.2)
        val finalScoreInt = totalScore.roundToInt()

        // 결과 AccessibilityInfo로 반환함
        return AccessibilityInfo(
            status = determineStatus(finalScoreInt),
            safetyScore = finalScoreInt,
            planAToiletId = planA?.first?.id?.toString(),
            planBToiletId = planB?.first?.id?.toString()
        )
    }


    private fun getScoreForTimeA(timeInMinutes: Int): Int = when {
        timeInMinutes <= 5 -> 100
        timeInMinutes <= 10 -> 70
        timeInMinutes <= 15 -> 50
        timeInMinutes <= 20 -> 20
        else -> 0
    }

    private fun getScoreForCount(count: Int): Int = when {
        count >= 3 -> 100
        count == 2 -> 80
        count == 1 -> 50
        else -> 0
    }

    private fun getScoreForTimeB(timeInMinutes: Int): Int = when {
        timeInMinutes <= 10 -> 100
        timeInMinutes <= 15 -> 70
        timeInMinutes <= 20 -> 40
        else -> 0
    }

    // 총점에 따른 최종 등급 컷 (기준은 유동적으로 수정 가능)
    private fun determineStatus(score: Int): AccessibilityStatus = when {
        score >= 70 -> AccessibilityStatus.GOOD      // 예: 70점 이상이면 안전
        score >= 40 -> AccessibilityStatus.WARNING   // 예: 40점 이상이면 주의
        else -> AccessibilityStatus.BAD              // 그 이하는 위험
    }

    // --- 🌐 위경도 좌표 기반 실제 거리(미터) 계산 (Haversine 공식) ---
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // 지구 반지름 (미터)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}