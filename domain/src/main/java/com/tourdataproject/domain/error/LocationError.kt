package com.tourdataproject.domain.error

sealed class LocationError {
    object PermissionDenied: LocationError()
    object GpsError: LocationError()
}