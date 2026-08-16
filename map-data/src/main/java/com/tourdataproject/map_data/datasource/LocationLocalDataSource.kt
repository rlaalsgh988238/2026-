package com.tourdataproject.map_data.datasource

interface LocationLocalDataSource {
    // 로컬 DB에서 저장된 좌표를 가져오는 함수 (비동기)
     suspend fun getUserLocation(): Pair<Double, Double>? // (경도, 위도)
}