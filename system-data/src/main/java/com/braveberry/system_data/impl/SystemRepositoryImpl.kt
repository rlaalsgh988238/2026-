package com.braveberry.system_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.system_data.dataSource.SystemDatasource
import com.braveberry.system_data.mapper.toJsonString
import com.braveberry.system_data.mapper.toPlanBackup
import com.tourdataproject.domain.model.PlanBackup
import com.tourdataproject.domain.repository.SystemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runInterruptible
import javax.inject.Inject

class SystemRepositoryImpl @Inject constructor(
    private val systemDatasource: SystemDatasource
): SystemRepository {
    override fun isDatabaseInit(): Flow<DataResource<Unit>> =
        systemDatasource.checkDatabaseInit()

    override fun getRestoredPlanState(): Flow<DataResource<PlanBackup?>> = flow<DataResource<PlanBackup?>> {
        emit(DataResource.Loading())
        val result = systemDatasource.loadBackUp()
        emit(DataResource.success(result?.toPlanBackup()))
    }.catch { e->
        emit(DataResource.Error(e))
    }

    override fun savePlanStateBackup(backup: PlanBackup): Flow<DataResource<Unit>> = flow{
        emit(DataResource.loading())
        systemDatasource.storeBackUp(backup.toJsonString())
        emit(DataResource.success(Unit))
    }

    override fun clearStateBackup(): Flow<DataResource<Unit>> = flow{
        emit(DataResource.loading())
        systemDatasource.clearBackUp()
        emit(DataResource.success(Unit))
    }
}