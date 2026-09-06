package com.tourdataproject.domain.usecase.plan.backUp

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.PlanBackup
import com.tourdataproject.domain.repository.SystemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SavePlanStateBackupUseCase @Inject constructor(
    private val repository: SystemRepository
) {
    operator fun invoke(backup: PlanBackup): Flow<DataResource<Unit>> =
        repository.savePlanStateBackup(backup)
}