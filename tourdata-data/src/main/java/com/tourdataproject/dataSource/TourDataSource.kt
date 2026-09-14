package com.tourdataproject.dataSource

import com.braveberry.data_resource.DataResource
import com.tourdataproject.model.TourDataModel
import kotlinx.coroutines.flow.Flow

interface TourDataSource {
    fun getContentId(
        lat: Double,
        lng: Double,
        radius: Int
    ): Flow<DataResource<String>>

    fun getTourDataToiletInfo(contentId: String): Flow<DataResource<TourDataModel>>
}