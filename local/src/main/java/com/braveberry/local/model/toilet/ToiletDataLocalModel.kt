package com.braveberry.local.model.toilet

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braveberry.local.mapper.LocalMapper
import com.braveberry.local.roomDB.RoomConstant
import com.braveberry.toilet_data.model.ToiletDataModel

@Entity(tableName = RoomConstant.Table.TOILET)
internal data class ToiletDataLocalModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // 기본 정보
    val toiletName: String,           // 화장실명
    val roadAddress: String?,         // 소재지도로명주소
    val lotAddress: String?,          // 소재지지번주소
    val isUnisex: Boolean,            // 남녀공용화장실여부

    // 남성용 시설 수
    val maleToiletBowlCount: Int,     // 남성용-대변기수
    val maleUrinalCount: Int,         // 남성용-소변기수
    val maleDisabledToiletCount: Int, // 남성용-장애인용대변기수
    val maleDisabledUrinalCount: Int, // 남성용-장애인용소변기수
    val maleChildToiletCount: Int,    // 남성용-어린이용대변기수
    val maleChildUrinalCount: Int,    // 남성용-어린이용소변기수

    // 여성용 시설 수
    val femaleToiletBowlCount: Int,   // 여성용-대변기수
    val femaleDisabledToiletCount: Int,// 여성용-장애인용대변기수
    val femaleChildToiletCount: Int,  // 여성용-어린이용대변기수

    // 운영 및 관리 정보
    val managingAgency: String?,      // 관리기관명
    val phoneNumber: String?,         // 전화번호
    val openTime: String?,            // 개방시간

    // 위치 정보
    val latitude: Double,             // WGS84위도
    val longitude: Double,            // WGS84경도

    // 안전 및 편의 시설
    val emergencyBellExists: Boolean, // 비상벨설치여부
    val cctvExists: Boolean,          // 화장실입구CCTV설치유무
    val diaperChangingStationExists: Boolean, // 기저귀교환대유무

    // 기타
    val updateDate: String?           // 데이터기준일자
): LocalMapper<ToiletDataModel> {
    override fun toData(): ToiletDataModel =
        ToiletDataModel(
            id,
            toiletName,
            roadAddress,
            lotAddress,
            isUnisex,
            maleToiletBowlCount,
            maleUrinalCount,
            maleDisabledToiletCount,
            maleDisabledUrinalCount,
            maleChildToiletCount,
            maleChildUrinalCount,
            femaleToiletBowlCount,
            femaleDisabledToiletCount,
            femaleChildToiletCount,
            managingAgency,
            phoneNumber,
            openTime,
            latitude,
            longitude,
            emergencyBellExists,
            cctvExists,
            diaperChangingStationExists,
            updateDate
        )
}

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