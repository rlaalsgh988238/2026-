package com.braveberry.local.mapper

import com.braveberry.local.model.AccessibilityInfoLocalModel
import com.braveberry.local.model.CourseLocalModel
import com.braveberry.local.model.DayPlanLocalModel
import com.braveberry.local.model.ScheduleItemLocalModel
import com.braveberry.toilet_data.course_data.model.AccessibilityInfoDataModel
import com.braveberry.toilet_data.course_data.model.CourseDataModel
import com.braveberry.toilet_data.course_data.model.DayPlanDataModel
import com.braveberry.toilet_data.course_data.model.ScheduleItemDataModel

internal fun CourseDataModel.toLocalModel(): CourseLocalModel = CourseLocalModel(
    courseId = courseId,
    destination = destination,
    courseName = courseName,
    startDate = startDate,
    endDate = endDate,
    dayPlans = dayPlans.map { it.toLocalModel() }
)

internal fun DayPlanDataModel.toLocalModel(): DayPlanLocalModel = DayPlanLocalModel(
    dayNumber = dayNumber,
    date = date,
    schedules = schedules.map { it.toLocalModel() }
)

internal fun ScheduleItemDataModel.toLocalModel(): ScheduleItemLocalModel = ScheduleItemLocalModel(
    scheduleId = scheduleId,
    order = order,
    scheduleName = scheduleName,
    visitTime = visitTime,
    memo = memo,
    latitude = latitude,
    longitude = longitude,
    placeId = placeId,
    address = address,
    category = category,
    accessibilityInfo = accessibilityInfo.toLocalModel()
)

internal fun AccessibilityInfoDataModel.toLocalModel(): AccessibilityInfoLocalModel = AccessibilityInfoLocalModel(
    status = status,
    safetyScore = safetyScore,
    planAToiletId = planAToiletId,
    planBToiletId = planBToiletId
)
