package com.tourdataproject.domain.model

import com.tourdataproject.domain.model.course.TravelCourse
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PlanBackup(
    val course: TravelCourse,
    val currentAddingDayNumber: Int,
    val draftStartDate: Long?,
    val draftEndDate: Long?
)