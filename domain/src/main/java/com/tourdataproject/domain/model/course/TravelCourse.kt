package com.tourdataproject.domain.model.course

data class TravelCourse(
    val courseId: String,
    val destination: String,
    val courseName: String,
    val startDate: Long,
    val endDate: Long,
    val dayPlans: List<DayPlan>
)