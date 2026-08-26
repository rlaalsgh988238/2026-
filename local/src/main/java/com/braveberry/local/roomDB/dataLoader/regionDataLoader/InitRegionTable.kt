package com.braveberry.local.roomDB.dataLoader.regionDataLoader

import android.content.Context
import com.braveberry.local.model.region.RegionDataLocalModel
import com.braveberry.local.roomDB.dao.RegionDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

internal suspend fun initRegionTableFromCsv(context: Context, regionDao: RegionDataDao) {
    withContext(Dispatchers.IO) {
        if (regionDao.getAnyRegion() != null) return@withContext

        val inputStream = context.assets.open("국토교통부_전국 법정동_20260630.csv")
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        val localDataList = mutableListOf<RegionDataLocalModel>()

        val popularExactMatches = listOf(
            "서울특별시" to null,
            "대전광역시" to null,
            "충청북도" to "청주시",
            "인천광역시" to null,
            "경기도" to "수원시",
            "대구광역시" to null,
            "부산광역시" to null,
            "전북특별자치도" to "전주시",
            "전남광주통합특별시" to null,
            "전남광주통합특별시" to "나주시",
            "제주특별자치도" to null,
            "경상남도" to "거제시"
        )

        reader.readLine() // 헤더 스킵

        reader.forEachLine { line ->
            val tokens = line.split(",")

            if (tokens.size >= 5) {
                val province = tokens[1].trim()
                val city = tokens[2].trim().takeIf { it.isNotBlank() }
                val town = tokens[3].trim().takeIf { it.isNotBlank() }
                val village = tokens[4].trim().takeIf { it.isNotBlank() }

                // 🌟 읍/면/동/리가 없는 "최상위 행정구역"이면서, 12개 목록에 정확히 일치할 때만 true
                val isPop = town == null && village == null &&
                        popularExactMatches.contains(province to city)

                val localData = RegionDataLocalModel(
                    code = tokens[0].trim(),
                    province = province,
                    city = city,
                    town = town,
                    village = village,
                    isPopular = isPop
                )
                localDataList.add(localData)
            }
        }

        localDataList.chunked(1000).forEach { chunk ->
            regionDao.insertList(chunk)
        }
    }
}
