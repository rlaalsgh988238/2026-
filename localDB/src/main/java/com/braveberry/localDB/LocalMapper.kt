package com.braveberry.localDB

interface LocalMapper<DataModel> {
    fun toData(): DataModel
}