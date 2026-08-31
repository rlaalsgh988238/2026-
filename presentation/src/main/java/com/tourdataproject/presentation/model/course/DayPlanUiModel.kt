package com.tourdataproject.presentation.model.course

import com.tourdataproject.domain.model.course.DayPlan
import com.tourdataproject.presentation.mapper.UiMapper
import com.tourdataproject.presentation.mapper.mapListToDomain
import com.tourdataproject.presentation.mapper.toUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayPlanUiModel(
    val dayLabel: String,   // "1일차"
    val dateLabel: String,  // "8/30"
    val rawDayNumber: Int,  // 1
    val rawDate: Long,      // 1693353600000L
    val schedules: List<ScheduleItemUiModel>
) {
    fun toDomain(): DayPlan {
        return DayPlan(
            dayNumber = this.rawDayNumber,
            date = this.rawDate,
            schedules = this.schedules.map { it.toDomain() }
        )
    }
}

