package com.tourdataproject.presentation.mapper

inline fun <UiModel, DomainModel> List<UiModel>.mapListToDomain(toDomain: (UiModel) -> DomainModel): List<DomainModel> {
    return this.map(toDomain)
}