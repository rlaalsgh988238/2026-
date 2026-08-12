package com.braveberry.toilet_data

import com.braveberry.toilet_data.model.ToiletEntity
import com.tourdataproject.domain.model.Toilet
import kotlin.collections.map

interface DataMapper<DomainModel> {
    fun toDomain(): DomainModel
}

internal fun <DataModel : DataMapper<DomainModel>, DomainModel> List<DataModel>.toDomain(): List<DomainModel> {
    return map { it.toDomain() }
}

internal fun Toilet.toData(): ToiletEntity {
    return ToiletEntity(
        id = this.id,
        toiletName = this.toiletName,
        roadAddress = this.roadAddress,
        lotAddress = this.lotAddress,
        isUnisex = this.isUnisex,
        maleToiletBowlCount = this.maleToiletBowlCount,
        maleUrinalCount = this.maleUrinalCount,
        maleDisabledToiletCount = this.maleDisabledToiletCount,
        maleDisabledUrinalCount = this.maleDisabledUrinalCount,
        maleChildToiletCount = this.maleChildToiletCount,
        maleChildUrinalCount = this.maleChildUrinalCount,
        femaleToiletBowlCount = this.femaleToiletBowlCount,
        femaleDisabledToiletCount = this.femaleDisabledToiletCount,
        femaleChildToiletCount = this.femaleChildToiletCount,
        managingAgency = this.managingAgency,
        phoneNumber = this.phoneNumber,
        openTime = this.openTime,
        latitude = this.latitude,
        longitude = this.longitude,
        emergencyBellExists = this.emergencyBellExists,
        cctvExists = this.cctvExists,
        diaperChangingStationExists = this.diaperChangingStationExists,
        updateDate = this.updateDate
    )
}
