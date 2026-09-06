package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.TempState
import kotlinx.coroutines.flow.Flow

interface ViewModelTempDataRepository {
    fun hasTempState(): Flow<DataResource<Boolean>>
    fun saveTempState(data: String, route: String): Flow<DataResource<Boolean>>
    fun getTempState(): Flow<DataResource<TempState>>
    fun clearTempState(): Flow<DataResource<Boolean>>
}