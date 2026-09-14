package com.tourdataproject.domain.usecase.plan.backUp

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.repository.SystemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClearPlanStateBackupUseCase @Inject constructor(
    private val repository: SystemRepository
) {
    operator fun invoke(): Flow<DataResource<Unit>> =
        repository.clearPlanStateBackup()
}