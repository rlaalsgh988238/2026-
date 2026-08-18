package com.tourdataproject.domain.usecase.user_data

import com.braveberry.data_resource.DataResource
import com.braveberry.data_resource.collectDataResource
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.repository.KakaoMapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TrackUserLocationUseCase @Inject constructor(private val kakaoMapRepository: KakaoMapRepository){
    operator fun invoke(): Flow<DataResource<Location>> = flow{
        kakaoMapRepository.getUserLocation().collect {
            when(it){
                is DataResource.Success -> {
                    emit(it)
                }
                is DataResource.Error -> {
                    TODO("권한, GPS 등 오류 로직")
                }
                is DataResource.Loading -> {
                    emit(it)
                }
            }
        }
    }
}