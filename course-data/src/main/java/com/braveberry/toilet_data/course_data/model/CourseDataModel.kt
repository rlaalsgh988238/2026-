package com.braveberry.toilet_data.course_data.model


data class CourseDataModel(
    val courseId: String,
    val destination: String,
    val courseName: String,
    val startDate: Long,
    val endDate: Long,
    val dayPlans: List<DayPlanDataModel>
)



data class DayPlanDataModel(
    val dayNumber: Int,
    val date: Long,
    val schedules: List<ScheduleItemDataModel>
)
data class ScheduleItemDataModel(
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
    val accessibilityInfo: AccessibilityInfoDataModel
)

data class AccessibilityInfoDataModel(
    val status: String, // DB나 서버 저장을 위해 Enum 대신 String 사용 흠
    val safetyScore: Int,
    val planAToiletId: String?,
    val planBToiletId: String?
)

