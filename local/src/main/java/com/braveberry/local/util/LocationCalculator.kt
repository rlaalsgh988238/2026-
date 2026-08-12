package com.braveberry.local.util

import javax.inject.Inject

internal class LocationCalculator @Inject constructor() {
    // 한국 위도 기준 상수 (km를 m로 변경: 111.0 * 1000, 88.8 * 1000)
    private val LAT_DEGREE_METERS = 111000.0
    private val LNG_DEGREE_METERS = 88800.0

    // dist 파라미터도 미터(m) 단위를 받는다고 가정합니다.
    fun getMinLat(lat: Double, distInMeters: Float) = lat - (distInMeters / LAT_DEGREE_METERS)
    fun getMaxLat(lat: Double, distInMeters: Float) = lat + (distInMeters / LAT_DEGREE_METERS)
    fun getMinLng(lng: Double, distInMeters: Float) = lng - (distInMeters / LNG_DEGREE_METERS)
    fun getMaxLng(lng: Double, distInMeters: Float) = lng + (distInMeters / LNG_DEGREE_METERS)
}