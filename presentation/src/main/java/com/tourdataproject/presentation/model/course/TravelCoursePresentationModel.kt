package com.tourdataproject.presentation.model.course

import com.tourdataproject.domain.model.course.TravelCourse
import com.tourdataproject.presentation.mapper.UiMapper
import com.tourdataproject.presentation.mapper.mapListToDomain

data class TravelCoursePresentationModel(
    val courseId: String = "",    //고유값
    val destination: String = "", // 여기 기준으로 검색 필터링 (필요하다면?)
    val destinationLatitude: Double = 0.0,
    val destinationLongitude: Double = 0.0,
    val courseName: String = "",  //여행 이름
    val datePeriod: String = "", //n박n일
    val rawStartDate: Long = 0,
    val rawEndDate: Long = 0,
    val dayPlans: List<DayPlanUiModel> = emptyList()
) : UiMapper<TravelCourse> {

    override fun toDomain(): TravelCourse {
        return TravelCourse(
            courseId = this.courseId,
            destination = this.destination,
            courseName = this.courseName,
            startDate = this.rawStartDate,
            endDate = this.rawEndDate,
            dayPlans = this.dayPlans.mapListToDomain { it.toDomain() }
        )
    }
}

