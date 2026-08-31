package com.braveberry.system_data.dataSource

import com.braveberry.data_resource.DataResource
import kotlinx.coroutines.flow.Flow

interface SystemDatasource {
    fun checkDatabaseInit(): Flow<DataResource<Unit>>
}