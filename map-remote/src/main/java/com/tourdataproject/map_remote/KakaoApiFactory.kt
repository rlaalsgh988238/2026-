package com.tourdataproject.map_remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
// map-remote 모듈 내부
object KakaoApiFactory {
    private const val BASE_URL = "https://dapi.kakao.com/"
    private val KAKAO_REST_API_KEY = KakaoMapApiGetter().getKey()

    fun createRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "KakaoAK $KAKAO_REST_API_KEY")
                    .build()
                chain.proceed(request)
            }
            // 로깅 인터셉터 등 추가
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}