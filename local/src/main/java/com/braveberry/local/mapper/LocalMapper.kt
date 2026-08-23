package com.braveberry.local.mapper

import com.braveberry.local.model.ToiletDataLocalModel
import com.braveberry.toilet_data.model.ToiletDataModel

interface LocalMapper<DataModel> {
    fun toData(): DataModel
}

internal fun <LocalModel : LocalMapper<DataModel>, DataModel> List<LocalModel>.toData(): List<DataModel> {
    return map { it.toData() }
}

// ---------------------------------------------------------


