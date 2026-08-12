package com.tourdataproject.domain.usecase.getToIlet

import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
import com.tourdataproject.domain.repository.ToiletRepository
import javax.inject.Inject

/**
 * 화장실 이름으로 검색하는 UseCase
 * 실시간으로 화장실 리스트 업데이트할 수 있음
 * 디바운싱은 프레젠테이션 레이어에서 처리
 */
class GetToiletsByNameUseCase @Inject constructor(
    private val toiletRepository: ToiletRepository
) {
    operator fun invoke(name: String) =
        toiletRepository.getToiletsByName(name)
            .onSuccess {

            }
            .onError {

            }
}