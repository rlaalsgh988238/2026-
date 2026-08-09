package com.tourdataproject.map_remote

import com.tourdataproject.map_remote.api.KakaoMapApi
import com.tourdataproject.map_remote.api.factory.KakaoApiFactory
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KakaoRealNetworkTest {

    private val logger = KotlinLogging.logger {}
    private lateinit var api: KakaoMapApi

    @Before
    fun setUp() {
        // 💡 팩토리 코드를 개선한 덕분에 setUp이 이렇게나 짧아졌습니다!
        // 파라미터 없이 호출만 하면 기본 URL과 BuildConfig의 API 키가 자동으로 세팅됩니다.
        val retrofit = KakaoApiFactory.createRetrofit()
        api = retrofit.create(KakaoMapApi::class.java)
    }

    @Test
    fun `실제 카카오 장소 검색 API를 호출하고 정상적인 DTO를 반환받는다`() = runBlocking {
        logger.info { "실제 카카오 서버 통신 테스트 시작" }

        // When: 실제 "하남시"를 키워드로 장소 검색 API 호출 (페이지 1)
        val response = api.getSearch(query = "하남시", page = 1)

        // Then: 1. HTTP 상태 코드가 200번대(성공)인지 확인
        assertTrue("통신이 성공해야 합니다. (API 키 오류 시 401 에러 발생)", response.isSuccessful)

        // Then: 2. 응답 Body가 정상적으로 SearchResponseDto로 파싱되었는지 확인
        val responseBody = response.body()
        assertNotNull("응답 Body가 null이 아니어야 합니다", responseBody)

        logger.info { "✅ 통신 성공! 상태 코드: ${response.code()}" }

        // Then: 3. 검색 결과 데이터가 잘 들어있는지 확인
        assertTrue("검색 결과(documents)가 비어있지 않아야 합니다", responseBody!!.documents.isNotEmpty())

        // 첫 번째 장소의 이름을 로그로 출력하여 눈으로 직접 확인합니다.
        val firstPlaceName = responseBody.documents[0].addressName
        logger.info { "🔍 첫 번째 검색 결과: $firstPlaceName" }
    }
}