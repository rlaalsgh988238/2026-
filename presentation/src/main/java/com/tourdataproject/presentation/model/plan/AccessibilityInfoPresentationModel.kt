package com.tourdataproject.presentation.model.plan

import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.presentation.mapper.PresentationMapper

data class AccessibilityInfoPresentationModel(
    val status: AccessibilityStatusPresentationModel = AccessibilityStatusPresentationModel.UNKNOWN,
    val safetyScore: Int = 0,
    val planAToiletId: String? = null,
    val planBToiletId: String? = null
) : PresentationMapper<AccessibilityInfo> {
    override fun toDomain(): AccessibilityInfo {
        return AccessibilityInfo(
            status = this.status.toDomain(),
            safetyScore = this.safetyScore,
            planAToiletId = this.planAToiletId,
            planBToiletId = this.planBToiletId
        )
    }
}