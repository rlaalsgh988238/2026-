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
import com.tourdataproject.presentation.model.course.TravelCourseUiModel
import java.text.SimpleDateFormat
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
fun TravelCourse.toUiModel(): TravelCourseUiModel {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
    return TravelCourseUiModel(
        courseId = this.courseId,
        destination = this.destination,
        courseName = this.courseName,
        datePeriod = "${dateFormat.format(Date(this.startDate))} ~ ${dateFormat.format(Date(this.endDate))}",
        rawStartDate = this.startDate,
        rawEndDate = this.endDate,
        dayPlans = this.dayPlans.map { it.toUiModel() }
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