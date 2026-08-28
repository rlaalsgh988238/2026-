package com.tourdataproject.presentation.model.course

import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.presentation.mapper.UiMapper

enum class AccessibilityStatusUiModel : UiMapper<AccessibilityStatus> {
    GOOD, WARNING, BAD;
    override fun toDomain(): AccessibilityStatus = when (this) {
        GOOD -> AccessibilityStatus.GOOD
        WARNING -> AccessibilityStatus.WARNING
        BAD -> AccessibilityStatus.BAD
    }
}