package com.braveberry.local.model

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
    val startDate: Long,
    val endDate: Long,
    val dayPlans: List<DayPlanLocalModel>
) : LocalMapper<CourseDataModel> {
    override fun toData(): CourseDataModel = CourseDataModel(
        courseId = courseId,
        destination = destination,
        courseName = courseName,
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
        accessibilityInfo = accessibilityInfo.toData()
    )
}

data class AccessibilityInfoLocalModel(
    val status: String,
    val safetyScore: Int,
    val planAToiletId: String?,
    val planBToiletId: String?
) : LocalMapper<AccessibilityInfoDataModel> {
    override fun toData(): AccessibilityInfoDataModel = AccessibilityInfoDataModel(
        status = status,
        safetyScore = safetyScore,
        planAToiletId = planAToiletId,
        planBToiletId = planBToiletId
    )
}