package com.tourdataproject.presentation.model.plan

import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.presentation.mapper.UiMapper

data class AccessibilityInfoPresentationModel(
    val status: AccessibilityStatusPresentationModel = AccessibilityStatusPresentationModel.UNKNOWN,
    val safetyScore: Int? = 0,
    val planAToiletId: String? = null,
    val planBToiletId: String? = null,
    // 🌟 UI 화면에 띄워줄 무장애 정보들 추가 (기본값 null)
    val parking: String? = null,
    val route: String? = null,
    val elevator: String? = null,
    val restroom: String? = null,
    val wheelchair: String? = null,
    val exit: String? = null
) : UiMapper<AccessibilityInfo> {
    override fun toDomain(): AccessibilityInfo {
        return AccessibilityInfo(
            status = this.status.toDomain(),
            safetyScore = this.safetyScore,
            planAToiletId = this.planAToiletId,
            planBToiletId = this.planBToiletId,
            parking = this.parking,
            route = this.route,
            elevator = this.elevator,
            restroom = this.restroom,
            wheelchair = this.wheelchair,
            exit = this.exit
        )
    }
}