package com.braveberry.local.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.local.roomDB.AppDatabase
import com.braveberry.local.roomDB.DatabaseRegistrationManager
import com.braveberry.system_data.dataSource.SystemDatasource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// SystemDataSourceImpl.kt 수정
class SystemDataSourceImpl @Inject internal constructor(
    private val registrationManager: DatabaseRegistrationManager,
    private val database: AppDatabase // 이거 지우면 안됨!! 스플래시에서는 이거 있어야 데이터베이스 실행됨
) : SystemDatasource {
    override fun checkDatabaseInit(): Flow<DataResource<Unit>> {
        return registrationManager.isReady.map { isReady ->
            if (isReady) {
                DataResource.Success(Unit)
            } else {
                DataResource.Loading()
            }
        }
    }
}

