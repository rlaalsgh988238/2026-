package com.tourdataproject.domain.usecase.getToIlet

import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
import com.tourdataproject.domain.model.Toilet
import com.tourdataproject.domain.repository.ToiletRepository
import javax.inject.Inject

/**
 * null값 나오면 해당 화장실 없음
 */
class GetToiletsByIdUseCase @Inject constructor(
    private val toiletRepository: ToiletRepository
) {
    operator fun invoke(id: String) =
        toiletRepository.getToiletById(id)
            .onSuccess {

            }
            .onError {

            }
}