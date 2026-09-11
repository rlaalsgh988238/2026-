package com.tourdataproject.presentation.model.plan

import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.presentation.mapper.PresentationMapper

enum class AccessibilityStatusPresentationModel : PresentationMapper<AccessibilityStatus> {
    GOOD, WARNING, BAD, UNKNOWN;
    override fun toDomain(): AccessibilityStatus = when (this) {
        GOOD -> AccessibilityStatus.GOOD
        WARNING -> AccessibilityStatus.WARNING
        BAD -> AccessibilityStatus.BAD
        UNKNOWN -> AccessibilityStatus.UNKNOWN
    }
}