package com.tourdataproject.domain.usecase.user_data

import com.tourdataproject.domain.repository.KakaoMapRepository
import javax.inject.Inject

class GetUserLocationUseCase @Inject constructor(private val kakaoMapRepository: KakaoMapRepository){
    operator fun invoke() = kakaoMapRepository.getUserLocation()
}