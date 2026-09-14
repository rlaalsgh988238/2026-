package com.tourdataproject.model

data class TourDataModel(
    val contentId: String,

    // 무장애 정보 필드들
    val parking: String?,
    val route: String?,
    val elevator: String?,
    val restroom: String?,
    val wheelchair: String?,
    val exit: String?
)