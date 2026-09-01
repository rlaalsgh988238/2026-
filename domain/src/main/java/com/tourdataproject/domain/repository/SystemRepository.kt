package com.tourdataproject.domain.repository

import com.braveberry.data_resource.DataResource
import kotlinx.coroutines.flow.Flow

interface SystemRepository {
    fun isDatabaseInit(): Flow<DataResource<Unit>>
}