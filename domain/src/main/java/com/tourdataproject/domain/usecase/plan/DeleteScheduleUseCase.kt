package com.tourdataproject.domain.usecase.plan

import com.tourdataproject.domain.model.course.DayPlan
import javax.inject.Inject

class DeleteScheduleUseCase @Inject constructor() {
    operator fun invoke(
        currentPlans: List<DayPlan>,
        targetDay: Int,
        scheduleIdToRemove: String
    ): List<DayPlan> {
        return currentPlans.map { dayPlan ->
            if (dayPlan.dayNumber == targetDay) {
                dayPlan.copy(schedules = dayPlan.schedules.filterNot { it.scheduleId == scheduleIdToRemove })
            } else dayPlan
        }
    }
}
