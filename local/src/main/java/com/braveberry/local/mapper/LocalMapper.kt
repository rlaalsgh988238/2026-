package com.braveberry.local.mapper

import com.braveberry.local.model.ToiletDataLocal
import com.braveberry.toilet_data.model.ToiletEntity

interface LocalMapper<DataModel> {
    fun toData(): DataModel
}

internal fun <LocalModel : LocalMapper<DataModel>, DataModel> List<LocalModel>.toData(): List<DataModel> {
    return map { it.toData() }
}

// ---------------------------------------------------------

internal fun ToiletEntity.toLocal(): ToiletDataLocal = ToiletDataLocal(
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

internal fun List<ToiletEntity>.toLocal(): List<ToiletDataLocal> {
    return map { it.toLocal() }
}
