package com.braveberry.toilet_data.dataSource

import com.braveberry.toilet_data.model.ToiletDataModel

interface ToiletDataSource {
    suspend fun getToiletData(toiletId: String): ToiletDataModel?
    suspend fun getSimilarNameToiletData(name: String): List<ToiletDataModel>
    suspend fun getAllToiletData(): List<ToiletDataModel>
    suspend fun getToiletDataInBox(distance: Float, latitude: Double, longitude: Double): List<ToiletDataModel>
    suspend fun insertToiletData(toiletDataModel: ToiletDataModel)
    suspend fun insertToiletDataList(toiletDataModelList: List<ToiletDataModel>)
}