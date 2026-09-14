package com.braveberry.local.provider

import android.annotation.SuppressLint
import com.braveberry.data_resource.DataResource
import com.braveberry.local.model.location.LocationLocalModel
import com.braveberry.local.permission.PermissionChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@SuppressLint("MissingPermission")
internal class LocalLocationProvider @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val permissionChecker: PermissionChecker
) {
    internal fun provideUserLocation(): Flow<DataResource<LocationLocalModel>> =
        if (permissionChecker.hasLocationPermission())
            callbackFlow {
                trySend(DataResource.loading())

                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val model = LocationLocalModel(
                                latitude = location.latitude,
                                longitude = location.longitude
                            )
                            trySend(DataResource.success(model))
                        } else {
                            trySend(DataResource.error(Exception("GPS 오류: 위치를 찾을 수 없음")))
                        }
                        close()
                    }
                    .addOnFailureListener { exception ->
                        trySend(DataResource.error(exception))
                        close()
                    }
                awaitClose { }
            }
        else {
            flowOf(DataResource.error(Exception("권한 거부")))
        }
}