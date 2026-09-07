package com.tourdataproject.domain.model.course

import kotlinx.serialization.Serializable

@Serializable
data class AccessibilityInfo(
    val status: AccessibilityStatus,
    val safetyScore: Int,
    val planAToiletId: String?,
    val planBToiletId: String?,
    //관광데이터 -> 무장애여행 model 추가

    val parking: String?,
    val route: String?,
    val elevator: String?,
    val restroom: String?,
    val wheelchair: String?,
    val exit: String?
)