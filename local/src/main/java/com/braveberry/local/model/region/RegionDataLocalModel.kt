package com.braveberry.local.model.region

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.braveberry.local.mapper.LocalMapper
import com.braveberry.local.roomDB.RoomConstant
import com.tourdataproject.map_data.model.RegionDataModel

@Entity(tableName = RoomConstant.Table.REGION)
internal data class RegionDataLocalModel(
    @PrimaryKey val code: String,     // 법정동코드 (10자리)
    val province: String,             // 시도명 (예: 서울특별시, 전라남도)
    val city: String?,                // 시군구명 (예: 종로구, 여수시)
    val town: String?,                // 읍면동명 (예: 청운동, 돌산읍)
    val village: String?,             // 리명 (예: 군내리)
    val isPopular: Boolean = false    // 인기 도시 여부
) : LocalMapper<RegionDataModel> {
    override fun toData(): RegionDataModel =
        RegionDataModel(
            code = code,
            province = province,
            city = city,
            town = town,
            village = village,
            isPopular = isPopular
        )
}

internal fun RegionDataModel.toLocal(): RegionDataLocalModel = RegionDataLocalModel(
    code = code,
    province = province,
    city = city,
    town = town,
    village = village,
    isPopular = isPopular
)
