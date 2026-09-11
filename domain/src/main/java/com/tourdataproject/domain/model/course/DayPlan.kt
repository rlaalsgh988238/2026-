package com.tourdataproject.domain.model.course

import kotlinx.serialization.Serializable

@Serializable
data class DayPlan(
    val dayNumber: Int,
    val date: Long,
    val schedules: List<ScheduleItem>,
    val stay: ScheduleItem? = null
)