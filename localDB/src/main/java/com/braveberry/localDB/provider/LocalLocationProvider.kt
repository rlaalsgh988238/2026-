package com.braveberry.localDB.provider

import android.annotation.SuppressLint
import com.braveberry.localDB.model.LocationLocalModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocalLocationProvider @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationAsByteArray(): ByteArray? {
        return try {
            // 코루틴의 await()를 사용해 비동기로 마지막 위치를 가져옵니다.
            val location = fusedLocationClient.lastLocation.await()

            if (location != null) {
                // 1. 위치가 있으면 모델에 담기
                val model = LocationLocalModel(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                // 2. 단일화된 ByteArray 규격으로 강제 직렬화하여 반환
                val jsonString = Gson().toJson(model)
                jsonString.toByteArray(Charsets.UTF_8)
            } else {
                null // GPS가 꺼져있거나 위치를 못 찾으면 null 반환
            }
        } catch (e: Exception) {
            null
        }
    }
}