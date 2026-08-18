package com.tourdataproject.domain.model.course

data class ScheduleItem(
    val scheduleId: String,
    val order: Int,
    val scheduleName: String,
    val visitTime: String?, //시간을 통해서 화장실 계산
    val memo: String?,

    // --- 지도/위치 관련 데이터 ---
    val latitude: Double,
    val longitude: Double,
    val placeId: String?,
    val address: String?,
    val category: String?,         //정보 불러올때

    // --- 화장실 및 접근성 데이터 ---
    val accessibilityInfo: AccessibilityInfo
)