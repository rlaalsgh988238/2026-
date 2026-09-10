package com.tourdataproject.domain.model

import com.tourdataproject.domain.model.course.ScheduleItem
import com.tourdataproject.domain.model.course.TravelCourse
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PlanBackup(
    val currentAddingDayNumber: Int,
    val course: TravelCourse,
    val draftStartDate: Long?,
    val draftEndDate: Long?,
    val draftScheduleItem: ScheduleItem?
)