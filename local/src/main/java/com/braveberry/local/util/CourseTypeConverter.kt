package com.braveberry.local.util

import androidx.room.TypeConverter
import com.braveberry.local.model.course.DayPlanLocalModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class CourseTypeConverter {
    private val gson = Gson()

    // 코스를 DB에 저장할 때
    @TypeConverter
    fun fromDayPlanList(dayPlans: List<DayPlanLocalModel>): String {
        return gson.toJson(dayPlans)
    }

    @TypeConverter
    fun toDayPlanList(jsonString: String): List<DayPlanLocalModel> {
        val type = object : TypeToken<List<DayPlanLocalModel>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}