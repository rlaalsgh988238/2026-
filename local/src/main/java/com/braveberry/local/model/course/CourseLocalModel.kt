package com.braveberry.local.model.course

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braveberry.local.mapper.LocalMapper
import com.braveberry.local.roomDB.RoomConstant
import com.braveberry.toilet_data.course_data.model.AccessibilityInfoDataModel
import com.braveberry.toilet_data.course_data.model.CourseDataModel
import com.braveberry.toilet_data.course_data.model.DayPlanDataModel
import com.braveberry.toilet_data.course_data.model.ScheduleItemDataModel

@Entity(tableName = RoomConstant.Table.COURSE)
data class CourseLocalModel(
    @PrimaryKey val courseId: String,
    val destination: String,
    val courseName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val startDate: Long,
    val endDate: Long,
    val dayPlans: List<DayPlanLocalModel>
) : LocalMapper<CourseDataModel> {
    override fun toData(): CourseDataModel = CourseDataModel(
        courseId = courseId,
        destination = destination,
        courseName = courseName,
        destinationLatitude = destinationLatitude,
        destinationLongitude = destinationLongitude,
        startDate = startDate,
        endDate = endDate,
        dayPlans = dayPlans.map { it.toData() }
    )
}

data class DayPlanLocalModel(
    val dayNumber: Int,
    val date: Long,
    val schedules: List<ScheduleItemLocalModel>
) : LocalMapper<DayPlanDataModel> {
    override fun toData(): DayPlanDataModel = DayPlanDataModel(
        dayNumber = dayNumber,
        date = date,
        schedules = schedules.map { it.toData() }
    )
}

data class ScheduleItemLocalModel(
    val scheduleId: String,
    val order: Int,
    val scheduleName: String,
    val visitTime: String?,
    val memo: String?,
    val latitude: Double,
    val longitude: Double,
    val placeId: String?,
    val address: String?,
    val category: String?,
    val contentId : String?,
    val accessibilityInfo: AccessibilityInfoLocalModel
) : LocalMapper<ScheduleItemDataModel> {
    override fun toData(): ScheduleItemDataModel = ScheduleItemDataModel(
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
        accessibilityInfo = accessibilityInfo.toData(),
        contentId=this.contentId
    )
}

data class AccessibilityInfoLocalModel(
    val status: String,
    val safetyScore: Int?,
    val planAToiletId: String?,
    val planBToiletId: String?,
    val parking: String?,
    val route: String?,
    val elevator: String?,
    val restroom: String?,
    val wheelchair: String?,
    val exit: String?
) : LocalMapper<AccessibilityInfoDataModel> {
    override fun toData(): AccessibilityInfoDataModel = AccessibilityInfoDataModel(
        status = status,
        safetyScore = safetyScore,
        planAToiletId = planAToiletId,
        planBToiletId = planBToiletId,
        parking = parking,
        route = route,
        elevator = elevator,
        restroom = restroom,
        wheelchair = wheelchair,
        exit = exit
    )
}