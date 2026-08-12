package com.braveberry.toilet_data.utiltiy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterCalculatorTest {

    private val calculator = FilterCalculator()

    @Test
    fun `isInCircle_returnsTrue_whenPointIsInsideRadius`() {
        val centerLat = 37.5546
        val centerLng = 126.9706
        // 1.0f 대신 1000.0f로 바꿔서 실행해 보세요.
        val radius = 1000.0f


        val targetLat = 37.5591
        val targetLng = 126.9776

        // 실제 계산된 거리를 로그로 확인하고 싶다면 FilterCalculator에 거리 반환 함수가 있어야 합니다.
        // 일단 결과값만 확인합니다.
        val result = calculator.isInCircle(centerLat, centerLng, targetLat, targetLng, radius)

        println("계산 결과: $result")
        assertTrue("1km 반경 내에 있으므로 true여야 함", result)
    }


    @Test
    fun `isInCircle_returnsFalse_whenPointIsOutsideRadius`() {
        // Given: 중심점 (서울역 인근)
        val centerLat = 37.5546
        val centerLng = 126.9706
        val radiusInKm = 1.0f

        // When: 약 2km 떨어진 지점 (명동 인근 너머)
        val targetLat = 37.5661
        val targetLng = 126.9873

        val result = calculator.isInCircle(centerLat, centerLng, targetLat, targetLng, radiusInKm)

        // Then
        assertFalse("1km 반경을 벗어났으므로 false여야 함", result)
    }

    @Test
    fun `isInCircle_returnsTrue_whenPointIsExactlyOnEdge`() {
        // 경계선 근처 테스트
        val centerLat = 37.0
        val centerLng = 127.0
        val radiusInKm = 0.0f // 반경이 0일 때

        val result = calculator.isInCircle(centerLat, centerLng, centerLat, centerLng, radiusInKm)

        // 자기 자신은 반경이 0이라도 포함되어야 함
        assertTrue(result)
    }


}