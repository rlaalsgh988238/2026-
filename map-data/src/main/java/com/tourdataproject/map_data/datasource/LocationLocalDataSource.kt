package com.tourdataproject.map_data.datasource

interface LocationLocalDataSource {
     suspend fun getUserLocation(): Pair<Double, Double>? // (경도, 위도)
}