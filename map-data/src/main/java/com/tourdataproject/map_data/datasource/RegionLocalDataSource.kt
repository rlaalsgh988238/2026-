package com.tourdataproject.map_data.datasource

import com.braveberry.data_resource.DataResource
import com.tourdataproject.map_data.model.RegionDataModel

interface RegionLocalDataSource {
    suspend fun getPopularRegion(): List<RegionDataModel>
}