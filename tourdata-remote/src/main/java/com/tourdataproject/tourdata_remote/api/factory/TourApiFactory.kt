
package com.tourdataproject.tourdata_remote.api.factory
/*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.tourdataproject.tourdata_remote.BuildConfig
object TourApiFactory {

    // 파라미터 기본값으로 BuildConfig의 안전한 변수를 사용합니다. (하드코딩 탈출!)
    fun createRetrofit(
        baseUrl: String = BuildConfig.TOUR_BASE_URL,
        apiKey: String = BuildConfig.TOUR_API_KEY
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                // 🌟 공공데이터 공통 쿼리 파라미터 및 인증키 톨게이트 자동 주입
                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter("MobileOS", "AND")
                    .addQueryParameter("MobileApp", "TourDataProject") // 앱 이름
                    .addQueryParameter("_type", "json")
                    // 공공데이터는 이미 % 기호로 인코딩된 키이므로 반드시 Encoded 함수 사용
                    .addEncodedQueryParameter("serviceKey", apiKey)
                    .build()

                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .build()

                chain.proceed(newRequest)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

 */