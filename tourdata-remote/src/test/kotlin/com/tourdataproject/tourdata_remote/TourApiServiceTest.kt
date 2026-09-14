package com.tourdataproject.tourdata_remote.api

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TourApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: TourApiService

    @Before
    fun setUp() {
        // 1. 가짜 웹 서버 시작
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // 2. 가짜 서버의 URL을 BaseUrl로 사용하는 Retrofit 생성
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TourApiService::class.java)
    }

    @After
    fun tearDown() {
        // 테스트가 끝나면 서버 종료
        mockWebServer.shutdown()
    }

    @Test
    fun `위치기반 관광정보 조회 성공 시 LocationItem 리스트를 반환한다`() = runTest {
        // Given: API가 응답할 가짜 JSON 데이터 세팅 (우리가 만든 KtoApiResponse 구조와 일치해야 함)
        val mockJson = """
            {
              "response": {
                "header": { "resultCode": "0000", "resultMsg": "OK" },
                "body": {
                  "numOfRows": 10,
                  "pageNo": 1,
                  "totalCount": 1,
                  "items": {
                    "item": [
                      { "contentid": "12345", "title": "가덕휴게소", "dist": "50" }
                    ]
                  }
                }
              }
            }
        """.trimIndent()

        // 가짜 서버가 위의 JSON을 반환하도록 설정
        mockWebServer.enqueue(MockResponse().setBody(mockJson).setResponseCode(200))

        // When: API 호출
        val response = apiService.getLocationBasedList(
            mapX = "127.123",
            mapY = "37.123",
            radius = "100"
        )

        // Then: 제네릭 껍데기를 뚫고 알맹이가 잘 매핑되었는지 검증
        val items = response.response.body?.items?.item
        assertNotNull(items)
        assertEquals(1, items?.size)

        val firstItem = items!![0]
        assertEquals("12345", firstItem.contentId)
        assertEquals("가덕휴게소", firstItem.title)
        assertEquals("50", firstItem.dist)
    }

    @Test
    fun `무장애여행 상세조회 성공 시 DetailWithTourItem 리스트를 반환한다`() = runTest {
        // Given: 무장애 정보 가짜 JSON 세팅
        val mockJson = """
            {
              "response": {
                "header": { "resultCode": "0000", "resultMsg": "OK" },
                "body": {
                  "numOfRows": 10,
                  "pageNo": 1,
                  "totalCount": 1,
                  "items": {
                    "item": [
                      { 
                        "contentid": "12345", 
                        "parking": "장애인 주차장 있음", 
                        "restroom": "장애인 화장실 있음",
                        "elevator": "엘리베이터 있음"
                      }
                    ]
                  }
                }
              }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(mockJson).setResponseCode(200))

        // When: API 호출
        val response = apiService.getDetailWithTour(contentId = "12345")

        // Then: 편의시설 데이터 파싱 검증
        val items = response.response.body?.items?.item
        assertNotNull(items)
        assertEquals(1, items?.size)

        val detailItem = items!![0]
        assertEquals("12345", detailItem.contentId)
        assertEquals("장애인 주차장 있음", detailItem.parking)
        assertEquals("장애인 화장실 있음", detailItem.restroom)
        assertEquals("엘리베이터 있음", detailItem.elevator)
    }
}