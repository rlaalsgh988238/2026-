package com.braveberry.local.mapper

import com.braveberry.local.model.LocationLocalModel
import com.google.gson.Gson
import javax.inject.Inject

class LocalLocationMapper @Inject constructor() {
    fun mapFromByteArray(rawData: ByteArray): Pair<Double, Double>? {
        return try {
            val jsonString = String(rawData, Charsets.UTF_8)

            val model = Gson().fromJson(jsonString, LocationLocalModel::class.java)

            Pair(model.longitude, model.latitude)

        } catch (e: Exception) {
            null
        }
    }
}