package com.tourdataproject.domain.usecase.plan

import com.tourdataproject.domain.model.course.DayPlan
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateCourseDatesUseCase @Inject constructor() {

    data class Result(
        val startMillis: Long,
        val endMillis: Long,
        val dayPlans: List<DayPlan>
    )

    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Result {
        val zone = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = endDate.atStartOfDay(zone).toInstant().toEpochMilli()

        // 총 여행 일수 계산
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1

        // 도메인 모델(DayPlan) 리스트 생성
        val generatedDayPlans = (0 until totalDays).map { i ->
            val currentDate = startDate.plusDays(i.toLong())
            val currentDateMillis = currentDate.atStartOfDay(zone).toInstant().toEpochMilli()

            DayPlan(
                dayNumber = i + 1,
                date = currentDateMillis,
                schedules = emptyList()
            )
        }

        return Result(startMillis, endMillis, generatedDayPlans)
    }
}
