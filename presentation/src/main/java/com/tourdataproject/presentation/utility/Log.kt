package com.tourdataproject.presentation.utility

import android.util.Log

object Log {
    private const val BASE_TAG = "MyTag_Presentation"
    private const val END_TAG = "_MyTag"

    // Debug
    fun d(msg: String) {
        Log.d(BASE_TAG, msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(tag + END_TAG, msg)
    }

    // Error
    fun e(msg: String) {
        Log.e(BASE_TAG, msg)
    }

    fun e(tag: String, msg: String) {
        Log.e(tag + END_TAG, msg)
    }

    fun e(tag: String, msg: String, e: Throwable) {
        Log.e(tag + END_TAG, msg, e)
    }

    // Info
    fun i(msg: String) {
        Log.i(BASE_TAG, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(tag + END_TAG, msg)
    }
}
