package com.tourdataproject.domain.usecase.calculateAceesibility

import com.tourdataproject.domain.model.course.AccessibilityStatus
import javax.inject.Inject
import kotlin.math.*

class AccessibilityCalculator @Inject constructor() {

    companion object {
        private const val WALKING_SPEED_PER_MINUTE = 67.0
        private const val EARTH_RADIUS = 6371000.0
    }

    fun calculateWalkingTime(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceMeters = EARTH_RADIUS * c

        return (distanceMeters / WALKING_SPEED_PER_MINUTE).toInt()
    }

    fun calculateFinalScore(timeA: Int, count: Int, timeB: Int?): Pair<Int, AccessibilityStatus> {
        val scoreA = when {
            timeA <= 5 -> 100
            timeA <= 10 -> 70
            timeA <= 15 -> 50
            timeA <= 20 -> 20
            else -> 0
        }

        val scoreCount = when {
            count >= 3 -> 100
            count == 2 -> 80
            count == 1 -> 50
            else -> 0
        }

        val scoreB = timeB?.let {
            when {
                it <= 10 -> 100
                it <= 15 -> 70
                it <= 20 -> 40
                else -> 0
            }
        } ?: 0

        val totalScore = ((scoreA * 0.6) + (scoreCount * 0.2) + (scoreB * 0.2)).roundToInt()

        val status = when {
            totalScore >= 70 -> AccessibilityStatus.GOOD
            totalScore >= 40 -> AccessibilityStatus.WARNING
            else -> AccessibilityStatus.BAD
        }

        return Pair(totalScore, status)
    }
}