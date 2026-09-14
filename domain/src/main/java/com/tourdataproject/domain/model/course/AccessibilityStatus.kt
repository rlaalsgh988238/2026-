package com.tourdataproject.domain.model.course

import kotlinx.serialization.Serializable

@Serializable
enum class AccessibilityStatus {
    GOOD, WARNING, BAD, UNKNOWN
}