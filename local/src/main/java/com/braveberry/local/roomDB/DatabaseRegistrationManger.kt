package com.braveberry.local.roomDB

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DatabaseRegistrationManager @Inject constructor() {
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    fun markAsReady() {
        Log.d("TOUR_DATA_DEBUG", "3. markAsReady called. Current State: true")
        _isReady.value = true
    }
}
