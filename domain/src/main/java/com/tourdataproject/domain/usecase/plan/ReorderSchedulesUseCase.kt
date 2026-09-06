package com.tourdataproject.domain.usecase.plan

import com.tourdataproject.domain.model.course.DayPlan
import com.tourdataproject.domain.model.course.ScheduleItem
import javax.inject.Inject

class ReorderSchedulesUseCase @Inject constructor() {
    operator fun invoke(
        currentPlans: List<DayPlan>,
        targetDay: Int,
        reorderedSchedules: List<ScheduleItem>
    ): List<DayPlan> {
        return currentPlans.map { dayPlan ->
            if (dayPlan.dayNumber == targetDay) {
                dayPlan.copy(schedules = reorderedSchedules)
            } else dayPlan
        }
    }
}
