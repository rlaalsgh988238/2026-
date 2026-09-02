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
            // city가 있으면 city를 쓰고, 없으면 province를 기준 삼음
            val target = city ?: province

            // 1. 텍스트 끝에 붙은 행정구역 단위 제거 (시, 군, 구, 특별시 등)
            // "청주시상당구" -> "청주시상당" -> "청주상당" 순으로 끝자리 단위를 안전하게 제거합니다.
            val regex = "(특별시|광역시|특별자치시|특별자치도|통합|시|군|구)$".toRegex()

            var result = target.replace(regex, "").trim()

            // 2. 만약 "청주시상당구"가 "청주시상당"을 거쳐 "청주상당"이 되었을 때,
            // 중간에 남아있을 수 있는 "시"나 "군"을 한 번 더 정리해 줍니다.
            // 예: "청주시상당" -> "청주 상당" (가독성을 위해 한 칸 띄어줌)
            if (result.contains("시")) {
                result = result.replace("시", " ").trim()
            } else if (result.contains("군")) {
                result = result.replace("군", " ").trim()
            }

            return result
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
