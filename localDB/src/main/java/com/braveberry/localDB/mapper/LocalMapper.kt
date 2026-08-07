package com.braveberry.localDB.mapper

import com.braveberry.localDB.model.ToiletDataLocal
import com.braveberry.toilet_data.model.ToiletEntity

// 1. 기존에 작성하신 Local -> Data 인터페이스와 확장 함수
interface LocalMapper<DataModel> {
    fun toData(): DataModel
}

internal fun <LocalModel : LocalMapper<DataModel>, DataModel> List<LocalModel>.toData(): List<DataModel> {
    return map { it.toData() }
}

// ---------------------------------------------------------

// 2. Data -> Local 변환을 위한 확장 함수 (insert용)
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

// 리스트 단위 insert를 위한 확장 함수
internal fun List<ToiletEntity>.toLocal(): List<ToiletDataLocal> {
    return map { it.toLocal() }
}
