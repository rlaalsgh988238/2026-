package com.tourdataproject.presentation.model

import com.tourdataproject.domain.model.Region

data class RegionUiModel(
    val code: String,
    val province: String,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val isPopular: Boolean = false
) {
    val shortName: String
        get() {
            val target = city ?: province
            return target.replace("특별시", "")
                .replace("광역시", "")
                .replace("특별자치시", "")
                .replace("특별자치도", "")
                .replace("통합", "")
                .replace("시", "")
                .replace("군", "")
                .trim()
        }
}


internal fun RegionUiModel.toDomain() =
    Region(
        code,
        province,
        city,
        town,
        village,
        isPopular
    )

internal fun Region.toUiModel() =
    RegionUiModel(
        code, province, city, town, village, isPopular
    )
