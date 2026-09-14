package com.tourdataproject.mapper

import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.domain.model.course.AccessibilityStatus
import com.tourdataproject.model.TourDataModel

fun AccessibilityInfo.toDataModel(contentId: String): TourDataModel {
    return TourDataModel(
        contentId = contentId,
        parking = this.parking,
        route = this.route,
        elevator = this.elevator,
        restroom = this.restroom,
        wheelchair = this.wheelchair,
        exit = this.exit
    )
}

fun TourDataModel.toDomainModel(): AccessibilityInfo {
    return AccessibilityInfo(
        // 앱 내부 비즈니스 로직을 위한 필드 초기화
        status = AccessibilityStatus.UNKNOWN, // 로직에 따라 나중에 갱신
        safetyScore = 0,
        planAToiletId = null,
        planBToiletId = null,

        // Data Model에서 가져온 무장애 정보 매핑
        parking = this.parking,
        route = this.route,
        elevator = this.elevator,
        restroom = this.restroom,
        wheelchair = this.wheelchair,
        exit = this.exit
    )
}