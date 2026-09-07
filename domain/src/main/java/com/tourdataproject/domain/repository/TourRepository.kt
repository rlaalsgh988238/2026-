package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.model.course.ScheduleItem
import kotlinx.coroutines.flow.Flow

interface TourRepository {
    fun getLocationBasedList(lat: Double, lng: Double, radius: Int): Flow<DataResource<List<ScheduleItem>>>
    fun getTourDataToiletInfo(contentId: String): Flow<DataResource<AccessibilityInfo>>
}