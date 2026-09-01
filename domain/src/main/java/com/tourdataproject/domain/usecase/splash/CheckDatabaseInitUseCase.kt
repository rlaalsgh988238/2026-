package com.tourdataproject.domain.usecase.splash

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.repository.SystemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckDatabaseInitUseCase @Inject constructor(private val systemRepository: SystemRepository) {
    operator fun invoke(): Flow<DataResource<Unit>> =
        systemRepository.isDatabaseInit()
}