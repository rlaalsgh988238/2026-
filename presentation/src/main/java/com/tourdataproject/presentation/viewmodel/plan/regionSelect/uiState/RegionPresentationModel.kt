package com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState

import com.tourdataproject.domain.model.Region

data class RegionPresentationModel(
    val code: String,
    val province: String,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val isPopular: Boolean = false
) {
    val exactName: String
        get(){
            city?.let {
                return "${province} ${city}"
            }
            return province
        }
    val shortName: String
        get() {
            // 1. 우선순위: town(읍면동) -> city(시군구) -> province(도)
            val target = town ?: city ?: province

            // 2. 광주·전남 특수 케이스 처리 (이미지상의 '특별' 문제 해결)
            // "전남광주통합특별시" 같은 긴 명칭이 들어오면 무조건 "광주"로 반환
            if (target.contains("광주") && target.contains("전남")) {
                return "광주"
            }

            // 3. 불필요한 수식어 제거 (통합 등)
            var result = target.replace("통합", "").trim()

            // 4. 행정구역 단위 제거 (긴 것부터)
            // 결과가 최소 2글자는 남아야 함 (시흥, 고흥, 경기 보호)
            val units = listOf("특별자치도", "특별자치시", "특별시", "광역시", "시", "군", "구", "도")

            for (unit in units) {
                if (result.endsWith(unit) && result.length > unit.length + 1) {
                    result = result.removeSuffix(unit)
                    break
                }
            }

            // 5. 시/군/구가 붙어있는 경우 처리 (예: 용인시기흥 -> 기흥)
            // 단, 결과가 2글자 이상일 때만 자름
            val cityIndex = result.indexOf("시")
            if (cityIndex != -1 && result.length > cityIndex + 1) {
                val sub = result.substring(cityIndex + 1)
                if (sub.length >= 2) result = sub
            }

            return result.trim()
        }
}




internal fun RegionPresentationModel.toDomain() =
    Region(
        code,
        province,
        city,
        town,
        village,
        isPopular
    )

internal fun Region.toUiModel() =
    RegionPresentationModel(
        code, province, city, town, village, isPopular
    )
