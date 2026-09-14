
package com.tourdataproject.tourdata_remote.api.factory

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.tourdataproject.tourdata_remote.BuildConfig
import com.tourdataproject.tourdata_remote.model.dto.KtoApiItems
import com.tourdataproject.tourdata_remote.model.dto.KtoApiItemsDeserializer
import okhttp3.logging.HttpLoggingInterceptor

object TourApiFactory {

    fun createRetrofit(
        baseUrl: String = BuildConfig.TOUR_BASE_URL,
        apiKey: String = BuildConfig.TOUR_API_KEY
    ): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter("MobileOS", "AND")
                    .addQueryParameter("MobileApp", "TourDataProject")
                    .addQueryParameter("_type", "json")
                    .addEncodedQueryParameter("serviceKey", apiKey)
                    .build()

                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .header("Connection", "close")
                    .build()

                chain.proceed(newRequest) // ✅ 원본(https) 요청 그대로 진행
            }
            .build()
        val gson = GsonBuilder()
            .registerTypeAdapter(KtoApiItems::class.java, KtoApiItemsDeserializer())
            .create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}

