package com.tourdataproject.map_remote

import com.tourdataproject.map_remote.api.KakaoMapApi
import com.tourdataproject.map_remote.api.factory.KakaoApiFactory
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class KakaoApiFactoryTest {

    private val logger = KotlinLogging.logger {}
    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: KakaoMapApi

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = KakaoApiFactory.createRetrofit(
            baseUrl = mockWebServer.url("/").toString(),
            apiKey = "TEST_MOCK_KEY" // 임의의 가짜 키
        )
        api = retrofit.create(KakaoMapApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `API 요청 시 Authorization 헤더에 API 키가 정상적으로 포함되어야 한다`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        api.getSearch(query = "테스트", page = 1)

        // Then: 가로챈 요청 헤더 확인
        val request = mockWebServer.takeRequest()
        val authHeader = request.getHeader("Authorization")

        logger.info { "🔍 가로챈 헤더 값: $authHeader" }

        // 우리가 팩토리에 주입했던 가짜 키가 제대로 헤더로 들어갔는지 검증
        val expectedKey = "KakaoAK TEST_MOCK_KEY"
        assertEquals("헤더에 API 키가 정확히 들어있어야 합니다", expectedKey, authHeader)
    }
}