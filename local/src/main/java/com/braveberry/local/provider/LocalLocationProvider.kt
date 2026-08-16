package com.braveberry.local.provider

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.braveberry.data_resource.DataResource
import com.braveberry.local.model.LocationLocalModel
import com.braveberry.local.permission.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@SuppressLint("MissingPermission")
internal class LocalLocationProvider @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val permissionChecker: PermissionChecker
) {
    // 🚨 위치 권한 승인은 UI(화면) 단에서 이미 받았다고 가정하고 에러를 무시하는 어노테이션입니다.
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

    internal fun provideUserLocation(): Flow<DataResource<LocationLocalModel>> =
        if (permissionChecker.hasLocationPermission())
            callbackFlow {
                trySend(DataResource.loading())

                val callback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let{
                            val model = LocationLocalModel(
                                latitude = it.latitude,
                                longitude = it.longitude
                            )
                            trySend(DataResource.success(model))
                        } ?: run {
                            trySend(DataResource.error(Exception("Location is null")))
                        }
                    }
                }

                awaitClose {
                    fusedLocationClient.removeLocationUpdates(callback)
                }
            }
        else {
            flowOf(DataResource.error(Exception("Permission Denied")))
        }
}