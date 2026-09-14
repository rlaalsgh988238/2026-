package com.tourdataproject.domain.usecase.plan

import com.tourdataproject.domain.model.course.DayPlan
import com.tourdataproject.domain.model.course.ScheduleItem
import javax.inject.Inject

class AddScheduleToDayUseCase @Inject constructor() {
    operator fun invoke(
        currentPlans: List<DayPlan>,
        targetDay: Int,
        newSchedule: ScheduleItem
    ): List<DayPlan> {
        return currentPlans.map { dayPlan ->
            if (dayPlan.dayNumber == targetDay) {
                dayPlan.copy(
                    schedules = dayPlan.schedules + newSchedule.copy(order = dayPlan.schedules.size + 1)
                )
            } else dayPlan
        }
    }
}
