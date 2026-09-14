package com.tourdataproject.presentation.model.plan

import com.tourdataproject.domain.model.course.DayPlan

data class DayPlanPresentationModel(
    val dayLabel: String = "",   // "1일차"
    val dateLabel: String = "",  // "8/30"
    val rawDayNumber: Int = 0,  // 1
    val rawDate: Long = 0,      // 1693353600000L
    val schedules: List<ScheduleItemPresentationModel> = emptyList(),
    val stay: ScheduleItemPresentationModel = ScheduleItemPresentationModel()
) {
    fun toDomain(): DayPlan {
        return DayPlan(
            dayNumber = this.rawDayNumber,
            date = this.rawDate,
            schedules = this.schedules.map { it.toDomain() },
            stay = this.stay.toDomain()
        )
    }
}

