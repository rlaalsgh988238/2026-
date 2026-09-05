package com.tourdataproject.presentation.mapper

import com.tourdataproject.domain.model.course.DayPlan
import com.tourdataproject.presentation.model.course.DayPlanUiModel

//요거랑 그냥 .map이랑 차이?? -> 플로우쪽 만들어야 하나? 라고 생각하다가 생각없이 만들어버림....
//TODO 이거 없애야한다......
inline fun <UiModel, DomainModel> List<UiModel>.mapListToDomain(toDomain: (UiModel) -> DomainModel): List<DomainModel> {
    return this.map(toDomain)
}

internal interface UiMapper<DomainModel> {
    fun toDomain(): DomainModel
}