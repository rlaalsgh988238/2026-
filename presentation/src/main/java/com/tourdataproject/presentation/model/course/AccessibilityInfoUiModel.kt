package com.tourdataproject.presentation.model.course

import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.presentation.mapper.UiMapper

data class AccessibilityInfoUiModel(
    val status: AccessibilityStatusUiModel,
    val safetyScore: Int,
    val planAToiletId: String?,
    val planBToiletId: String?
) : UiMapper<AccessibilityInfo> {
    override fun toDomain(): AccessibilityInfo {
        return AccessibilityInfo(
            status = this.status.toDomain(),
            safetyScore = this.safetyScore,
            planAToiletId = this.planAToiletId,
            planBToiletId = this.planBToiletId
        )
    }
}