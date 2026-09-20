package com.tourdataproject.map_remote.mapper

import androidx.annotation.Keep

@Keep
interface RemoteMapper<T> {
    fun toData(): T
}