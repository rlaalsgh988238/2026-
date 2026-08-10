package com.tourdataproject.domain.usecase.getToIlet

import com.tourdataproject.domain.model.Toilet
import com.tourdataproject.domain.repository.ToiletRepository
import javax.inject.Inject

class GetToiletsByIdUseCase @Inject constructor(
    private val toiletRepository: ToiletRepository
) {
    operator fun invoke(id: String) =
        toiletRepository.getToiletById(id)
}