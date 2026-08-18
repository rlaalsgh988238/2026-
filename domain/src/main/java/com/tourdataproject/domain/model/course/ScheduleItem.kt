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
    val accessibilityStatus: AccessibilityStatus, // 최종 아이콘 색상 (초록/주황)
    val safetyScore: Int,           // 🌟 추가: 0~100점으로 계산된 최종 안전 점수- >필요한지 생각
    val planAToiletId: String?,     // 🌟 변경: 1순위 화장실 ID ->조금 특별하게 보여주던가 하기
    val planBToiletId: String?,     // 🌟 변경: 2순위 화장실 ID (없으면 null) ->이것도 아이콘 특별하게 보여주기
)