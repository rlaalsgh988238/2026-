package com.tourdataproject.domain.model.course

import kotlinx.serialization.Serializable

@Serializable
data class AccessibilityInfo(
    val status: AccessibilityStatus,
    val safetyScore: Int,
    val planAToiletId: String?,
    val planBToiletId: String?
)