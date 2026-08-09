package com.tourdataproject.map_remote

import com.tourdataproject.map_remote.api.KakaoMapApi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // 사용하는 파서에 맞게 변경 (Gson/Moshi/Serialization 등)

// KkaoMapApi 형식 테스트
class KakaoMapApiTest {

    private val logger = KotlinLogging.logger {}
    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: KakaoMapApi

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(KakaoMapApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `카카오 장소 검색 API가 JSON을 SearchResponseDto로 정상 파싱한다`() = runBlocking { // 👈 suspend 함수 호출을 위해 runBlocking 사용
        logger.info { "🚀 실제 KakaoMapApi 파싱 테스트 시작!" }

        val mockJson = """
            {
                "documents": [
                    {
                        "address_name": "경기 하남시 미사동",
                        "x": "127.1938",
                        "y": "37.5684"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockJson)
        )

        val response = api.getSearch(query = "하남시", page = 1)

        assertTrue("통신이 성공해야 합니다", response.isSuccessful)

        val responseBody = response.body()
        assertNotNull("응답 Body(Dto)가 null이 아니어야 합니다", responseBody)


        logger.info { "✅ 성공적으로 DTO 변환 완료: $responseBody" }
    }
}
