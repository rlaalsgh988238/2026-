package com.tourdataproject.domain.model.course

data class DayPlan(
    val dayNumber: Int,
    val date: Long,
    val schedules: List<ScheduleItem>
)