package com.braveberry.local.roomDB.dataLoader.toiletDataLoader

import android.content.Context
import com.braveberry.local.model.toilet.ToiletDataLocalModel
import com.braveberry.local.roomDB.dao.ToiletDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

internal suspend fun initToiletTableFromCsv(context: Context, toiletDao: ToiletDataDao) {
    withContext(Dispatchers.IO) {
        if (toiletDao.getAllToiletData().isNotEmpty()) return@withContext

        val inputStream = context.assets.open("toiletData_updated.csv")
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        val localDataList = mutableListOf<ToiletDataLocalModel>()

        // 1. 헤더를 읽어서 각 데이터가 몇 번째 컬럼에 있는지 동적으로 파악합니다.
        val headerLine = reader.readLine() ?: return@withContext
        val headers = parseCsvLine(headerLine).map { it.trim() }

        val nameIdx = headers.indexOfFirst { it.contains("화장실명") }
        val roadAddrIdx = headers.indexOfFirst { it.contains("도로명주소") }
        val lotAddrIdx = headers.indexOfFirst { it.contains("지번주소") }
        val unisexIdx = headers.indexOfFirst { it.contains("남녀공용") }

        val mToiletIdx = headers.indexOfFirst { it.contains("남성용-대변기") }
        val mUrinalIdx = headers.indexOfFirst { it.contains("남성용-소변기") }
        val mDisToiletIdx = headers.indexOfFirst { it.contains("남성용-장애인용대변기") }
        val mDisUrinalIdx = headers.indexOfFirst { it.contains("남성용-장애인용소변기") }
        val mChildToiletIdx = headers.indexOfFirst { it.contains("남성용-어린이용대변기") }
        val mChildUrinalIdx = headers.indexOfFirst { it.contains("남성용-어린이용소변기") }

        val fToiletIdx = headers.indexOfFirst { it.contains("여성용-대변기") }
        val fDisToiletIdx = headers.indexOfFirst { it.contains("여성용-장애인용대변기") }
        val fChildToiletIdx = headers.indexOfFirst { it.contains("여성용-어린이용대변기") }

        val agencyIdx = headers.indexOfFirst { it.contains("관리기관명") }
        val phoneIdx = headers.indexOfFirst { it.contains("전화번호") }
        val openTimeIdx = headers.indexOfFirst { it.contains("개방시간") }

        val latIdx = headers.indexOfFirst { it.contains("위도") }
        val lngIdx = headers.indexOfFirst { it.contains("경도") }

        val bellIdx = headers.indexOfFirst { it.contains("비상벨") }
        val cctvIdx = headers.indexOfFirst { it.contains("CCTV") }
        val diaperIdx = headers.indexOfFirst { it.contains("기저귀") }
        val dateIdx = headers.indexOfFirst { it.contains("데이터기준일자") }

        // 2. 본문 데이터를 파싱합니다.
        reader.forEachLine { line ->
            val tokens = parseCsvLine(line)

            if (nameIdx != -1 && tokens.size > nameIdx) {
                val latStr = latIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it) } ?: "0.0"
                val lngStr = lngIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it) } ?: "0.0"

                val localData = ToiletDataLocalModel(
                    id = 0,
                    toiletName = tokens.getOrNull(nameIdx)?.trim() ?: "",
                    roadAddress = roadAddrIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.takeIf { s -> s.isNotBlank() } },
                    lotAddress = lotAddrIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.takeIf { s -> s.isNotBlank() } },
                    isUnisex = unisexIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim() == "Y" } ?: false,

                    maleToiletBowlCount = mToiletIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,
                    maleUrinalCount = mUrinalIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,
                    maleDisabledToiletCount = mDisToiletIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,
                    maleDisabledUrinalCount = mDisUrinalIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,
                    maleChildToiletCount = mChildToiletIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,
                    maleChildUrinalCount = mChildUrinalIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,

                    femaleToiletBowlCount = fToiletIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,
                    femaleDisabledToiletCount = fDisToiletIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,
                    femaleChildToiletCount = fChildToiletIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.toIntOrNull() } ?: 0,

                    managingAgency = agencyIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.takeIf { s -> s.isNotBlank() } },
                    phoneNumber = phoneIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.takeIf { s -> s.isNotBlank() } },
                    openTime = openTimeIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.takeIf { s -> s.isNotBlank() } },

                    latitude = latStr.toDoubleOrNull() ?: 0.0,
                    longitude = lngStr.toDoubleOrNull() ?: 0.0,

                    emergencyBellExists = bellIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim() == "Y" } ?: false,
                    cctvExists = cctvIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim() == "Y" } ?: false,
                    diaperChangingStationExists = diaperIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim() == "Y" } ?: false,

                    updateDate = dateIdx.takeIf { it != -1 }?.let { tokens.getOrNull(it)?.trim()?.takeIf { s -> s.isNotBlank() } }
                )
                localDataList.add(localData)
            }
        }

        toiletDao.insertList(localDataList)
    }
}

/**
 * 따옴표(") 안의 쉼표(,)는 무시하고 데이터를 분리하는 안전한 CSV 파서
 */
private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var inQuotes = false

    for (char in line) {
        if (char == '\"') {
            inQuotes = !inQuotes
        } else if (char == ',' && !inQuotes) {
            result.add(current.toString())
            current.clear()
        } else {
            current.append(char)
        }
    }
    result.add(current.toString())
    return result
}
