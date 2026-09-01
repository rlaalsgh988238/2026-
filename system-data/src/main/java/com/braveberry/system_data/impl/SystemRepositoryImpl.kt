package com.braveberry.system_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.system_data.dataSource.SystemDatasource
import com.tourdataproject.domain.repository.SystemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SystemRepositoryImpl @Inject constructor(
    private val systemDatasource: SystemDatasource
): SystemRepository {
    override fun isDatabaseInit(): Flow<DataResource<Unit>> =
        systemDatasource.checkDatabaseInit()
}