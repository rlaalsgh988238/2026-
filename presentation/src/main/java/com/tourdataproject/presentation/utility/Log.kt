package com.tourdataproject.presentation.utility

import android.util.Log

object Log {
    private const val BASE_TAG = "MyTag_Presentation"
    private const val PREFIX = "MyTag_"

    // Debug
    fun d(msg: String) {
        Log.d(BASE_TAG, msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(PREFIX + tag, msg)
    }

    // Error
    fun e(msg: String) {
        Log.e(BASE_TAG, msg)
    }

    fun e(tag: String, msg: String) {
        Log.e(PREFIX + tag, msg)
    }

    fun e(tag: String, msg: String, e: Throwable) {
        Log.e(PREFIX + tag, msg, e)
    }

    // Info
    fun i(msg: String) {
        Log.i(BASE_TAG, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(PREFIX + tag, msg)
    }
}
