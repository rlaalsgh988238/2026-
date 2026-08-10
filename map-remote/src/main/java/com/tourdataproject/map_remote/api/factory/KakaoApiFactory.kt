package com.tourdataproject.map_remote.api.factory
import com.tourdataproject.map_remote.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KakaoApiFactory {
    private const val DEFAULT_BASE_URL = "https://dapi.kakao.com/"

    fun createRetrofit(
        baseUrl: String = DEFAULT_BASE_URL,
        apiKey: String = BuildConfig.KAKAO_API_KEY
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "KakaoAK $apiKey")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}