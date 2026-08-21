package com.tourdataproject.domain.usecase.user_data

import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TrackUserLocationUseCase @Inject constructor(private val mapRepository: MapRepository){
    operator fun invoke(): Flow<DataResource<Location>> = flow{
        mapRepository.getUserLocation().collect { resource ->
            when(resource){
                is DataResource.Success -> {
                    emit(resource)
                }
                is DataResource.Error -> {
                    TODO("상황에 맞는 에러 처리")
                }
                is DataResource.Loading -> {
                    emit(resource)
                }
            }
        }
    }
}