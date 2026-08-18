package com.tourdataproject.domain.model.course

data class AccessibilityInfo(
    val status: AccessibilityStatus,
    val safetyScore: Int,
    val planAToiletId: String?,
    val planBToiletId: String?
)