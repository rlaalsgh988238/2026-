package com.tourdataproject.map_remote

import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// 1. 임시 테스트용 API 인터페이스
// (응답을 파싱하지 않고 ResponseBody로 날것의 JSON을 그대로 받습니다)
interface KakaoTestService {
    @GET("v2/local/search/keyword.json")
    fun searchKeyword(
        @Query("query") query: String,
        @Query("x") x: String? = null,      // 경도 (Longitude)
        @Query("y") y: String? = null,      // 위도 (Latitude)
        @Query("radius") radius: Int? = null // 검색 반경 (미터 단위, 최대 20000)
    ): Call<ResponseBody>
}
// 테스트 함수용 전역 로거 생성
private val logger = KotlinLogging.logger {}
fun main() {
    logger.info { "🚀 카카오 API Mock 위치 통신 테스트 시작!" }

    val retrofit = KakaoApiFactory.createRetrofit()
    val service = retrofit.create(KakaoTestService::class.java)

    // 강남역 좌표를 Mock 데이터로 넣고, 반경 1000m(1km) 이내로 설정
    val call = service.searchKeyword(
        query = "스타벅스",
        x = "127.0276",   // 경도
        y = "37.4979",    // 위도
        radius = 1000     // 1000미터
    )

    try {
        val response = call.execute()

        if (response.isSuccessful) {
            logger.info { "✅ 통신 성공! 상태 코드: ${response.code()}" }

            val jsonResult = response.body()?.string()
            logger.info { "결과 미리보기:\n${jsonResult?.take(700)}\n... (생략)" }
        } else {
            logger.error { "❌ 통신 에러: 상태 코드 ${response.code()}" }
            logger.error { "에러 내용: ${response.errorBody()?.string()}" }
        }
    } catch (e: Exception) {
        logger.error(e) { "💥 요청 실패" }
    }
}