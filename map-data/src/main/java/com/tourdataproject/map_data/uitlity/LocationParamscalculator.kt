package com.tourdataproject.map_data.uitlity // 패키지명 오타(uitlity -> utility) 나중에 한 번 확인해 보세요!

import com.tourdataproject.map_data.model.CalculateLocationParams
import com.tourdataproject.map_data.datasource.LocationLocalDataSource // 🔥 DataSource 임포트 추가!

internal suspend fun LocationLocalDataSource.calculateLocationParams(
    providedLng: Double?,
    providedLat: Double?,
    providedRadius: Int?
): CalculateLocationParams {

    val (targetLng, targetLat) = if (providedLng == null || providedLat == null) {
        this.getUserLocation() ?: Pair(null, null)
    } else {
        Pair(providedLng, providedLat)
    }

    val targetRadius = if (targetLng != null && targetLat != null) providedRadius ?: 2000 else null

    return CalculateLocationParams(targetLng, targetLat, targetRadius)
}
