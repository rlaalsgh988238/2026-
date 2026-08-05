package com.braveberry.localDB.impl

import com.braveberry.localDB.roomDB.dao.ToiletDataDao
import com.braveberry.toilet_data.localDB.ToiletDataSource
import com.braveberry.toilet_data.model.ToiletEntity
import javax.inject.Inject

class ToiletDBDataSourceImpl @Inject internal constructor(
    private val toiletDao: ToiletDataDao
): ToiletDataSource {
    override suspend fun getToiletData(toiletId: String): ToiletEntity? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllToiletData(): List<ToiletEntity> {
        TODO("Not yet implemented")
    }

}