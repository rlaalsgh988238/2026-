package com.tourdataproject.domain.model.course

import kotlinx.serialization.Serializable

@Serializable
data class TravelCourse(
    val courseId: String,
    val destination: String,
    val courseName: String,
    val startDate: Long,
    val endDate: Long,
    val dayPlans: List<DayPlan>
)