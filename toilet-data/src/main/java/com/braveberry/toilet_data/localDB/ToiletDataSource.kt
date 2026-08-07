package com.braveberry.toilet_data.localDB

import com.braveberry.toilet_data.model.ToiletEntity

interface ToiletDataSource {
    suspend fun getToiletData(toiletId: String): ToiletEntity?
    suspend fun getAllToiletData(): List<ToiletEntity>
    suspend fun insertToiletData(toiletEntity: ToiletEntity)
    suspend fun insertToiletDataList(toiletEntityList: List<ToiletEntity>)
}