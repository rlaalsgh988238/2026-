package com.tourdataproject.map_remote

//TODO: Model 타입 맞추기
interface RemoteMapper<T> {
    fun toData(): T
}