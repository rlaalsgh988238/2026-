package com.braveberry.toilet_data.course_data.mapper

import com.braveberry.toilet_data.course_data.model.AccessibilityInfoDataModel
import com.braveberry.toilet_data.course_data.model.CourseDataModel
import com.braveberry.toilet_data.course_data.model.DayPlanDataModel
import com.braveberry.toilet_data.course_data.model.ScheduleItemDataModel
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.domain.model.course.DayPlan
import com.tourdataproject.domain.model.course.ScheduleItem
import com.tourdataproject.domain.model.course.TravelCourse

fun CourseDataModel.toDomain(): TravelCourse {
    return TravelCourse(
        courseId = this.courseId,
        destination = this.destination,
        courseName = this.courseName,
        startDate = this.startDate,
        endDate = this.endDate,
        dayPlans = this.dayPlans.map { it.toDomain() } // 하위 리스트 연쇄 매핑
    )
}

fun DayPlanDataModel.toDomain(): DayPlan {
    return DayPlan(
        dayNumber = this.dayNumber,
        date = this.date,
        schedules = this.schedules.map { it.toDomain() }
    )
}

fun ScheduleItemDataModel.toDomain(): ScheduleItem {
    return ScheduleItem(
        scheduleId = this.scheduleId,
        order = this.order,
        scheduleName = this.scheduleName,
        visitTime = this.visitTime,
        memo = this.memo,
        latitude = this.latitude,
        longitude = this.longitude,
        placeId = this.placeId,
        address = this.address,
        category = this.category,
        accessibilityInfo = this.accessibilityInfo.toDomain()
    )
}

fun AccessibilityInfoDataModel.toDomain(): AccessibilityInfo {
    return AccessibilityInfo(
        //TODO: 에러나면 BAD인데 이것도 나중에 수정 생각
        status = runCatching { AccessibilityStatus.valueOf(this.status) }
            .getOrDefault(AccessibilityStatus.BAD),
        safetyScore = this.safetyScore,
        planAToiletId = this.planAToiletId,
        planBToiletId = this.planBToiletId
    )
}



//--------------------------------------------------------------------------
fun TravelCourse.toDataModel(): CourseDataModel {
    return CourseDataModel(
        courseId = this.courseId,
        destination = this.destination,
        courseName = this.courseName,
        startDate = this.startDate,
        endDate = this.endDate,
        dayPlans = this.dayPlans.map { it.toDataModel() }
    )
}

fun DayPlan.toDataModel(): DayPlanDataModel {
    return DayPlanDataModel(
        dayNumber = this.dayNumber,
        date = this.date,
        schedules = this.schedules.map { it.toDataModel() }
    )
}

fun ScheduleItem.toDataModel(): ScheduleItemDataModel {
    return ScheduleItemDataModel(
        scheduleId = this.scheduleId,
        order = this.order,
        scheduleName = this.scheduleName,
        visitTime = this.visitTime,
        memo = this.memo,
        latitude = this.latitude,
        longitude = this.longitude,
        placeId = this.placeId,
        address = this.address,
        category = this.category,
        accessibilityInfo = this.accessibilityInfo.toDataModel()
    )
}

fun AccessibilityInfo.toDataModel(): AccessibilityInfoDataModel {
    return AccessibilityInfoDataModel(
        status = this.status.name, // Enum -> String 변환
        safetyScore = this.safetyScore,
        planAToiletId = this.planAToiletId,
        planBToiletId = this.planBToiletId
    )
}