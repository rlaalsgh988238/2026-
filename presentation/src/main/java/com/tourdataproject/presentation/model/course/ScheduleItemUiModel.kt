package com.tourdataproject.presentation.model.course

import com.tourdataproject.domain.model.course.ScheduleItem
import com.tourdataproject.presentation.mapper.UiMapper

data class ScheduleItemUiModel(
    val scheduleId: String,
    val order: Int,
    val scheduleName: String,
    val visitTime: String,
    val memo: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String?,
    val address: String?,
    val category: String?,
    val accessibilityInfo: AccessibilityInfoUiModel // UI 모델 연결
) : UiMapper<ScheduleItem> {

    override fun toDomain(): ScheduleItem {
        return ScheduleItem(
            scheduleId = this.scheduleId,
            order = this.order,
            scheduleName = this.scheduleName,
            visitTime = this.visitTime.ifEmpty { null },
            memo = this.memo.ifEmpty { null },
            latitude = this.latitude,
            longitude = this.longitude,
            placeId = this.placeId,
            address = this.address,
            category = this.category,
            accessibilityInfo = this.accessibilityInfo.toDomain()
        )
    }
}