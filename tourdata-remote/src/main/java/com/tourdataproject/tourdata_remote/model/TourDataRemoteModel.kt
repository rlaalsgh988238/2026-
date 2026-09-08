package com.tourdataproject.tourdata_remote.model

import com.tourdataproject.model.TourDataModel


data class TourDataRemoteModel(
    val contentId: String,
    val parking: String?,
    val route: String?,
    val elevator: String?,
    val restroom: String?,
    val wheelchair: String?,
    val exit: String?
)
fun TourDataRemoteModel.toDataModel(): TourDataModel {
    return TourDataModel(
        contentId = this.contentId,
        parking = this.parking,
        route = this.route,
        elevator = this.elevator,
        restroom = this.restroom,
        wheelchair = this.wheelchair,
        exit = this.exit
    )
}