package com.tourdataproject.tourdata_remote.impl

import com.braveberry.data_resource.DataResource
import com.tourdataproject.tourdata_remote.api.TourApiService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TourDataSourceImplIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: TourApiService
    private lateinit var dataSource: TourDataSourceImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TourApiService::class.java)

        dataSource = TourDataSourceImpl(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getContentId 정상 응답 시 Loading 과 Success 순서로 발행된다`() = runTest {
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
                      { "contentid": "987654", "title": "테스트 장소", "dist": "150" }
                    ]
                  }
                }
              }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(mockJson).setResponseCode(200))

        val emissions = dataSource.getContentId(37.5, 127.0, 1000).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is DataResource.Loading)

        val successResult = emissions[1] as DataResource.Success
        assertEquals("987654", successResult.data)
    }

    @Test
    fun `getContentId 결과가 비어있으면 Loading 과 Error 순서로 발행된다`() = runTest {
        val mockJson = """
            {
              "response": {
                "header": { "resultCode": "0000", "resultMsg": "OK" },
                "body": {
                  "numOfRows": 10,
                  "pageNo": 1,
                  "totalCount": 0,
                  "items": {
                    "item": []
                  }
                }
              }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(mockJson).setResponseCode(200))

        val emissions = dataSource.getContentId(37.5, 127.0, 1000).toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is DataResource.Loading)
        assertTrue(emissions[1] is DataResource.Error)
    }

    @Test
    fun `getTourDataToiletInfo 무장애 정보 조회 성공 시 Loading 과 Success 순서로 발행된다`() = runTest {
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
                        "contentid": "987654", 
                        "parking": "장애인 주차구역 있음", 
                        "elevator": "엘리베이터 있음",
                        "restroom": "장애인 화장실 있음",
                        "route": null,
                        "wheelchair": null,
                        "exit": null
                      }
                    ]
                  }
                }
              }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(mockJson).setResponseCode(200))

        val emissions = dataSource.getTourDataToiletInfo("987654").toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is DataResource.Loading)

        val successResult = emissions[1] as DataResource.Success
        val dataModel = successResult.data
        assertEquals("987654", dataModel.contentId)
        assertEquals("장애인 주차구역 있음", dataModel.parking)
        assertEquals("엘리베이터 있음", dataModel.elevator)
    }
}
