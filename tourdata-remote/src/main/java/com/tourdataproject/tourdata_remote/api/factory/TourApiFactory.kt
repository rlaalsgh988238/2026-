
package com.tourdataproject.tourdata_remote.api.factory

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.tourdataproject.tourdata_remote.BuildConfig
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
                    .header("Connection", "close") //이거 없으면 터짐;
                    .build()

                val finalUrlString = newRequest.url.toString().replace("https://", "http://")
                val finalRequest = newRequest.newBuilder().url(finalUrlString).build()

                chain.proceed(finalRequest)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

