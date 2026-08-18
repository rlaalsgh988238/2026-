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

internal fun ToiletDataModel.toLocal(): ToiletDataLocalModel = ToiletDataLocalModel(
    id = id,
    toiletName = toiletName,
    roadAddress = roadAddress,
    lotAddress = lotAddress,
    isUnisex = isUnisex,
    maleToiletBowlCount = maleToiletBowlCount,
    maleUrinalCount = maleUrinalCount,
    maleDisabledToiletCount = maleDisabledToiletCount,
    maleDisabledUrinalCount = maleDisabledUrinalCount,
    maleChildToiletCount = maleChildToiletCount,
    maleChildUrinalCount = maleChildUrinalCount,
    femaleToiletBowlCount = femaleToiletBowlCount,
    femaleDisabledToiletCount = femaleDisabledToiletCount,
    femaleChildToiletCount = femaleChildToiletCount,
    managingAgency = managingAgency,
    phoneNumber = phoneNumber,
    openTime = openTime,
    latitude = latitude,
    longitude = longitude,
    emergencyBellExists = emergencyBellExists,
    cctvExists = cctvExists,
    diaperChangingStationExists = diaperChangingStationExists,
    updateDate = updateDate
)

internal fun List<ToiletDataModel>.toLocal(): List<ToiletDataLocalModel> {
    return map { it.toLocal() }
}
