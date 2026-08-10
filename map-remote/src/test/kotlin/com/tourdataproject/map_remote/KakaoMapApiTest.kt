package com.tourdataproject.map_remote

import com.tourdataproject.map_remote.api.KakaoMapApi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun `카카오 장소 검색 API가 JSON을 SearchResponseDto로 정상 파싱한다`() = runBlocking {
        logger.info { "🚀 실제 KakaoMapApi 파싱 테스트 시작!" }

        val mockJson = """
            {
                "meta": {
                    "is_end": false,
                    "pageable_count": 45,
                    "total_count": 100
                },
                "documents": [
                    {
                        "id": "123456789",
                        "place_name": "스타벅스 하남미사점",
                        "address_name": "경기 하남시 미사동 123",
                        "road_address_name": "경기 하남시 미사강변대로 123",
                        "x": "127.1938",
                        "y": "37.5684",
                        "distance": "1500",
                        "category_group_name": "카페",
                        "phone": "02-1234-5678",
                        "place_url": "http://place.map.kakao.com/123456789"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockJson)
        )

        // 🌟 2. 새로 뚫어둔 위치 기반 파라미터까지 전부 넣어서 테스트!
        val response = api.getSearch(
            query = "스타벅스",
            longitude = 127.1938,
            latitude = 37.5684,
            radius = 2000,
            page = 1
        )

        assertTrue("통신이 성공해야 합니다", response.isSuccessful)

        val responseBody = response.body()
        assertNotNull("응답 Body(Dto)가 null이 아니어야 합니다", responseBody)

        // 🌟 3. 우리가 DTO에 새로 추가한 값들이 제대로 매핑되었는지 검증!
        val firstDocument = responseBody!!.documents.first()
        assertEquals("ID가 일치해야 합니다", "123456789", firstDocument.id)
        assertEquals("장소 이름이 일치해야 합니다", "스타벅스 하남미사점", firstDocument.placeName)
        assertEquals("거리 값이 일치해야 합니다", "1500", firstDocument.distance)

        logger.info { "✅ 성공적으로 DTO 변환 완료 및 검증 통과: $responseBody" }
    }
}