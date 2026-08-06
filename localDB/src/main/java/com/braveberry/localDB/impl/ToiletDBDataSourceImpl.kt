package com.braveberry.localDB.impl

import com.braveberry.localDB.roomDB.dao.ToiletDataDao
import com.braveberry.localDB.mapper.toData
import com.braveberry.localDB.mapper.toLocal
import com.braveberry.toilet_data.localDB.ToiletDataSource
import com.braveberry.toilet_data.model.ToiletEntity
import javax.inject.Inject

class ToiletDBDataSourceImpl @Inject internal constructor(
    private val toiletDao: ToiletDataDao
): ToiletDataSource {
    override suspend fun getToiletData(toiletId: String): ToiletEntity? =
        toiletDao.getToiletData(toiletId)?.toData()

    override suspend fun getAllToiletData(): List<ToiletEntity> =
        toiletDao.getAllToiletData().toData()

    override suspend fun insertToiletData(toiletEntity: ToiletEntity) {
        toiletDao.insert(toiletEntity.toLocal())
    }

    override suspend fun insertToiletDataList(toiletEntityList: List<ToiletEntity>) {
        toiletDao.insertList(toiletEntityList.toLocal())
    }
}