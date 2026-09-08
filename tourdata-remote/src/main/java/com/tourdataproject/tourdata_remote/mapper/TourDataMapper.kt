package com.tourdataproject.tourdata_remote.mapper

import com.tourdataproject.tourdata_remote.model.TourDataRemoteModel
import com.tourdataproject.tourdata_remote.model.dto.DetailWithTourItem
import kotlin.collections.map

fun DetailWithTourItem.toRemoteModel(): TourDataRemoteModel {
    return TourDataRemoteModel(
        contentId = this.contentId,
        parking = this.parking,
        route = this.route,
        elevator = this.elevator,
        restroom = this.restroom,
        wheelchair = this.wheelchair,
        exit = this.exit
    )
}

// (선택) List 변환 편의 함수
fun List<DetailWithTourItem>.toRemoteModelList(): List<TourDataRemoteModel> {
    return this.map { it.toRemoteModel() }
}