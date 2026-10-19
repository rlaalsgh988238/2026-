package com.tourdataproject.presentation.model.plan

/** 편집 세션이 반환하는 변경 결과. UI Intent/Effect나 ViewModel에 의존하지 않습니다. */
data class ScheduleEditResult(
    val targetDay: Int,
    val schedules: List<ScheduleItemPresentationModel>,
    val deletedStayId: String? = null
)
