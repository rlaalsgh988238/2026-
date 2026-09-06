package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.PlanBackup
import kotlinx.coroutines.flow.Flow

interface SystemRepository {
    fun isDatabaseInit(): Flow<DataResource<Unit>>
    fun getStoredPlanState(): Flow<DataResource<PlanBackup?>>
    fun savePlanStateBackup(backup: PlanBackup): Flow<DataResource<Unit>>
    fun clearPlanStateBackup(): Flow<DataResource<Unit>>
}