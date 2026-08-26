package com.braveberry.local.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.local.mapper.toData
import com.braveberry.local.roomDB.dao.RegionDataDao
import com.tourdataproject.map_data.datasource.RegionLocalDataSource
import com.tourdataproject.map_data.model.RegionDataModel
import javax.inject.Inject

internal class RegionDataSourceImpl @Inject constructor(
    private val regionDataDao: RegionDataDao
): RegionLocalDataSource{
    override suspend fun getPopularRegion(): List<RegionDataModel> =
        regionDataDao.getPopularRegions().toData()
}