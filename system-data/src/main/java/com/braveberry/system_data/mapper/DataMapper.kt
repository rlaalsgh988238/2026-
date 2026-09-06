package com.braveberry.system_data.mapper

import com.tourdataproject.domain.model.PlanBackup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun PlanBackup.toJsonString(): String {
    return Json.encodeToString(this)
}

// JSON 문자열을 객체로 복구 (실패 시 null 반환)
fun String.toPlanBackup(): PlanBackup? {
    return try {
        Json.decodeFromString<PlanBackup>(this)
    } catch (e: Exception) {
        null
    }
}