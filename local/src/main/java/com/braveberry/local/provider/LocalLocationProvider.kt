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