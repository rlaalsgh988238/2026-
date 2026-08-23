package com.braveberry.local.impl

import com.braveberry.local.mapper.toData
import com.braveberry.local.roomDB.dao.ToiletDataDao
import com.braveberry.local.model.toilet.toLocal
import com.braveberry.local.util.LocationCalculator
import com.braveberry.toilet_data.dataSource.ToiletDataSource
import com.braveberry.toilet_data.model.ToiletDataModel
import javax.inject.Inject

internal class ToiletDBDataSourceImpl @Inject internal constructor(
    private val toiletDao: ToiletDataDao,
    private val locationCalculator: LocationCalculator
): ToiletDataSource {
    override suspend fun getToiletData(toiletId: String): ToiletDataModel? =
        toiletDao.getToiletData(toiletId)?.toData()

    override suspend fun getSimilarNameToiletData(name: String): List<ToiletDataModel> =
        toiletDao.getSimilarNameToiletData(name).toData()


    override suspend fun getAllToiletData(): List<ToiletDataModel> =
        toiletDao.getAllToiletData().toData()

    override suspend fun getToiletDataInBox(
        distance: Float,
        latitude: Double,
        longitude: Double
    ): List<ToiletDataModel> {
        val minLat = locationCalculator.getMinLat(latitude, distance)
        val maxLat = locationCalculator.getMaxLat(latitude, distance)
        val minLng = locationCalculator.getMinLng(longitude, distance)
        val maxLng = locationCalculator.getMaxLng(longitude, distance)

        return toiletDao.getToiletsInBox(minLat, maxLat, minLng, maxLng).toData()
    }

    override suspend fun insertToiletData(toiletDataModel: ToiletDataModel) {
        toiletDao.insert(toiletDataModel.toLocal())
    }

    override suspend fun insertToiletDataList(toiletDataModelList: List<ToiletDataModel>) {
        toiletDao.insertList(toiletDataModelList.toLocal())
    }
}