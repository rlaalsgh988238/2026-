package com.braveberry.local.roomDB.toiletDataLoader

import android.content.Context
import com.braveberry.local.model.toilet.ToiletDataLocalModel
import com.braveberry.local.roomDB.dao.ToiletDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

internal suspend fun initToiletTableFromCsv(context: Context, toiletDao: ToiletDataDao) {
    withContext(Dispatchers.IO) {
        // 이미 데이터가 있다면 파싱을 건너뛰는 로직을 추가하면 좋습니다.
        if (toiletDao.getAllToiletData().isNotEmpty()) return@withContext

        val inputStream = context.assets.open("toiletData.csv")
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        val localDataList = mutableListOf<ToiletDataLocalModel>()

        reader.readLine() // 헤더 스킵

        reader.forEachLine { line ->
            val tokens = line.split(",")

            if (tokens.size >= 23) {
                // ToiletEntity가 아닌 Room DB 모델(ToiletDataLocal)로 바로 파싱
                val localData = ToiletDataLocalModel(
                    id = 0,
                    toiletName = tokens[0],
                    roadAddress = tokens[1].takeIf { it.isNotBlank() },
                    lotAddress = tokens[2].takeIf { it.isNotBlank() },
                    isUnisex = tokens[3] == "Y",

                    maleToiletBowlCount = tokens[4].toIntOrNull() ?: 0,
                    maleUrinalCount = tokens[5].toIntOrNull() ?: 0,
                    maleDisabledToiletCount = tokens[6].toIntOrNull() ?: 0,
                    maleDisabledUrinalCount = tokens[7].toIntOrNull() ?: 0,
                    maleChildToiletCount = tokens[8].toIntOrNull() ?: 0,
                    maleChildUrinalCount = tokens[9].toIntOrNull() ?: 0,

                    femaleToiletBowlCount = tokens[10].toIntOrNull() ?: 0,
                    femaleDisabledToiletCount = tokens[11].toIntOrNull() ?: 0,
                    femaleChildToiletCount = tokens[12].toIntOrNull() ?: 0,

                    managingAgency = tokens[13].takeIf { it.isNotBlank() },
                    phoneNumber = tokens[14].takeIf { it.isNotBlank() },
                    openTime = tokens[15].takeIf { it.isNotBlank() },

                    latitude = tokens[16].toDoubleOrNull() ?: 0.0,
                    longitude = tokens[17].toDoubleOrNull() ?: 0.0,

                    emergencyBellExists = tokens[18] == "Y",
                    cctvExists = tokens[19] == "Y",
                    diaperChangingStationExists = tokens[20] == "Y",

                    updateDate = tokens[21].takeIf { it.isNotBlank() }
                )
                localDataList.add(localData)
            }
        }

        // Dao를 통해 바로 DB에 삽입
        toiletDao.insertList(localDataList)
    }
}
