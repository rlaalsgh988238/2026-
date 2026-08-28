package com.tourdataproject.presentation.mapper

//요거랑 그냥 .map이랑 차이??
inline fun <UiModel, DomainModel> List<UiModel>.mapListToDomain(toDomain: (UiModel) -> DomainModel): List<DomainModel> {
    return this.map(toDomain)
}
internal interface UiMapper<DomainModel> {
    fun toDomain(): DomainModel
}

