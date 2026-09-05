package com.tourdataproject.presentation.mapper

import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.domain.model.course.DayPlan
import com.tourdataproject.domain.model.course.ScheduleItem
import com.tourdataproject.domain.model.course.TravelCourse
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.AccessibilityStatusUiModel
import com.tourdataproject.presentation.model.course.DayPlanUiModel
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.model.course.TravelCoursePresentationModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun ScheduleItem.toUiModel(): ScheduleItemUiModel = ScheduleItemUiModel(
    scheduleId = this.scheduleId,
    order = this.order,
    scheduleName = this.scheduleName,
    visitTime = this.visitTime ?: "",
    memo = this.memo ?: "",
    latitude = this.latitude,
    longitude = this.longitude,
    placeId = this.placeId,
    address = this.address,
    category = this.category,
    accessibilityInfo = this.accessibilityInfo.toUiModel()
)

fun DayPlan.toUiModel(): DayPlanUiModel {
    val dateFormat = SimpleDateFormat("M/d", Locale.KOREA)
    return DayPlanUiModel(
        dayLabel = "${this.dayNumber}일차",
        dateLabel = dateFormat.format(Date(this.date)),
        rawDayNumber = this.dayNumber,
        rawDate = this.date,
        schedules = this.schedules.map { it.toUiModel() }
    )
}
//TODO: 기획에 맞게 수정
fun TravelCourse.toUiModel(): TravelCoursePresentationModel {
    // 1. 전체 시작일과 종료일을 꺼냅니다.
    val startLocalDate = Instant.ofEpochMilli(this.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val endLocalDate = Instant.ofEpochMilli(this.endDate).atZone(ZoneId.systemDefault()).toLocalDate()

    val dateLabelFormatter = DateTimeFormatter.ofPattern("M/dd")

    // 🌟 2. 상단 바에 보여줄 datePeriod ("yyyy.MM.dd ~ yyyy.MM.dd")를 다시 만듭니다!
    val periodFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val generatedDatePeriod = "${startLocalDate.format(periodFormatter)} ~ ${endLocalDate.format(periodFormatter)}"

    return TravelCoursePresentationModel(
        courseId = this.courseId,
        destination = this.destination,
        courseName = this.courseName,
        rawStartDate = this.startDate,
        rawEndDate = this.endDate,

        datePeriod = generatedDatePeriod,

        dayPlans = this.dayPlans.map { domainDayPlan ->
            val currentDayDate = startLocalDate.plusDays((domainDayPlan.dayNumber - 1).toLong())

            DayPlanUiModel(
                dayLabel = "${domainDayPlan.dayNumber}일차",
                dateLabel = currentDayDate.format(dateLabelFormatter),
                rawDayNumber = domainDayPlan.dayNumber,
                rawDate = currentDayDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                schedules = domainDayPlan.schedules.map { it.toUiModel() }
            )
        }
    )
}

fun AccessibilityInfo.toUiModel(): AccessibilityInfoUiModel {
    return AccessibilityInfoUiModel(
        status = this.status.toUiModel(),
        safetyScore = this.safetyScore,
        planAToiletId = this.planAToiletId,
        planBToiletId = this.planBToiletId
    )
}

fun AccessibilityStatus.toUiModel(): AccessibilityStatusUiModel = when (this) {
    AccessibilityStatus.GOOD -> AccessibilityStatusUiModel.GOOD
    AccessibilityStatus.WARNING -> AccessibilityStatusUiModel.WARNING
    AccessibilityStatus.BAD -> AccessibilityStatusUiModel.BAD
    AccessibilityStatus.UNKNOWN -> TODO()
}