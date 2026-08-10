package com.braveberry.local.impl

import com.braveberry.local.roomDB.dao.ToiletDataDao
import com.braveberry.local.mapper.toData
import com.braveberry.local.mapper.toLocal
import com.braveberry.toilet_data.localDB.ToiletDataSource
import com.braveberry.toilet_data.model.ToiletEntity
import javax.inject.Inject

internal class ToiletDBDataSourceImpl @Inject internal constructor(
    private val toiletDao: ToiletDataDao
): ToiletDataSource {
    override suspend fun getToiletData(toiletId: String): ToiletEntity? =
        toiletDao.getToiletData(toiletId)?.toData()

    override suspend fun getAllToiletData(): List<ToiletEntity> =
        toiletDao.getAllToiletData().toData()

    override suspend fun getToiletDataInBox(
        distance: Float,
        latitude: Double,
        longitude: Double
    ): List<ToiletEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun insertToiletData(toiletEntity: ToiletEntity) {
        toiletDao.insert(toiletEntity.toLocal())
    }

    override suspend fun insertToiletDataList(toiletEntityList: List<ToiletEntity>) {
        toiletDao.insertList(toiletEntityList.toLocal())
    }
}