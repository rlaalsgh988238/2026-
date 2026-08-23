package com.braveberry.local.mapper

interface LocalMapper<DataModel> {
    fun toData(): DataModel
}

internal fun <LocalModel : LocalMapper<DataModel>, DataModel> List<LocalModel>.toData(): List<DataModel> {
    return map { it.toData() }
}