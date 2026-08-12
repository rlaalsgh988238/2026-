package com.braveberry.toilet_data.utiltiy

import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal class FilterCalculator @Inject constructor() {
    fun isInCircle(
        centerLat: Double,
        centerLng: Double,
        targetLat: Double,
        targetLng: Double,
        radiusInMeters: Float
    ): Boolean {
        val earthRadius = 6371000.0 // 지구 반지름 (미터 단위)

        val dLat = Math.toRadians(targetLat - centerLat)
        val dLon = Math.toRadians(targetLng - centerLng)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(centerLat)) * cos(Math.toRadians(targetLat)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c

        return distance <= radiusInMeters
    }
}