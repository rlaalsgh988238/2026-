package com.braveberry.local.model.location

import com.google.gson.Gson
import com.tourdataproject.map_data.model.LocationDataModel
import javax.inject.Inject

internal class LocalLocationMapper @Inject constructor() {
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