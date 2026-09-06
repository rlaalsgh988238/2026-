package com.tourdataproject.tourdata_remote.api

import com.tourdataproject.tourdata_remote.api.factory.TourApiFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TourApiRealNetworkTest {

    // 🌟 1. 커넥션 풀 충돌을 막기 위해 클라이언트 인스턴스를 두 개로 완전히 분리
    private lateinit var apiService1: TourApiService
    private lateinit var apiService2: TourApiService

    @Before
    fun setUp() {
        val retrofit1 = TourApiFactory.createRetrofit()
        val retrofit2 = TourApiFactory.createRetrofit()

        apiService1 = retrofit1.create(TourApiService::class.java)
        apiService2 = retrofit2.create(TourApiService::class.java)
    }

    @Test
    fun `실제_네트워크_카카오좌표로_contentId조회_및_무장애정보_조회_연속테스트`() = runBlocking {

        // 1단계: 첫 번째 클라이언트로 위치기반 조회
        val locationResponse = apiService1.getLocationBasedList(
            mapX = "127.0276",
            mapY = "37.4979",
            radius = "2000"
        )

        val locationItems = locationResponse.response.body?.items?.item

        assertNotNull("위치기반 검색 실패: API 키나 네트워크 상태를 확인하세요.", locationItems)
        assertTrue("해당 반경 내에 관광공사 데이터가 없습니다.", locationItems!!.isNotEmpty())

        val targetPlace = locationItems[0]
        val targetContentId = targetPlace.contentId

        println("✅ 1단계 성공 - 타겟 장소명: ${targetPlace.title}, Content ID: $targetContentId")

        // 🌟 2. 공공데이터 서버의 연속 호출 차단을 피하고 소켓을 정리할 1.5초 대기 시간 부여
        delay(1500)

        // 2단계: 완전히 독립된 두 번째 클라이언트로 무장애 정보 조회
        val detailResponse = apiService2.getDetailWithTour(
            contentId = targetContentId
        )

        val detailItems = detailResponse.response.body?.items?.item

        assertNotNull("무장애 상세 정보 조회 실패", detailItems)
        assertTrue("무장애 상세 정보 리스트가 비어있습니다.", detailItems!!.isNotEmpty())

        val accessibilityInfo = detailItems[0]

        println("✅ 2단계 성공 - 무장애 정보 파싱 완료!")
        println(" - 장애인 주차장: ${accessibilityInfo.parking}")
        println(" - 장애인 화장실: ${accessibilityInfo.restroom}")
        println(" - 엘리베이터: ${accessibilityInfo.elevator}")
        println(" - 휠체어 대여: ${accessibilityInfo.wheelchair}")
    }
}