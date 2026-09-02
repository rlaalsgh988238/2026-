package com.braveberry.toilet_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.toilet_data.dataSource.ToiletDataSource
import com.braveberry.toilet_data.model.ToiletDataModel
import com.braveberry.toilet_data.utiltiy.FilterCalculator
import com.tourdataproject.domain.model.Toilet
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList // toList import 추가
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToiletRepositoryImplTest {

    private val dataSource: ToiletDataSource = mockk()
    private val filterCalculator: FilterCalculator = mockk()
    private val repository = ToiletRepositoryImpl(dataSource, filterCalculator)

    @Test
    fun getToiletsByDistance_filtersCorrectly() = runTest {
        val lat = 37.5
        val lng = 127.0
        val dist = 1.0f

        val inBox = createMockEntity(1, "In", 37.501, 127.001)
        val outBox = createMockEntity(2, "Out", 37.600, 128.000)

        coEvery { dataSource.getToiletDataInBox(dist, lat, lng) } returns listOf(inBox, outBox)
        every { filterCalculator.isInCircle(lat, lng, 37.501, 127.001, dist) } returns true
        every { filterCalculator.isInCircle(lat, lng, 37.600, 128.000, dist) } returns false

        // .first() 대신 .toList()를 사용하여 모든 emit 값을 수집합니다.
        val results = repository.getToiletsByDistance(dist, lat, lng).toList()

        // 첫 번째 emit은 Loading 상태여야 함
        assertTrue(results[0] is DataResource.Loading)

        // 두 번째 emit은 Success 상태여야 함
        assertTrue(results[1] is DataResource.Success)

        val data = (results[1] as DataResource.Success).data
        assertEquals(1, data.size)
        assertEquals("In", data[0].toiletName)
    }

    @Test
    fun getToiletsByDistance_returnsErrorOnFailure() = runTest {
        coEvery { dataSource.getToiletDataInBox(any(), any(), any()) } throws RuntimeException()

        // .first() 대신 .toList()를 사용합니다.
        val results = repository.getToiletsByDistance(1.0f, 37.0, 127.0).toList()

        // 첫 번째 emit은 Loading 상태여야 함
        assertTrue(results[0] is DataResource.Loading)

        // 두 번째 emit은 Error 상태여야 함
        assertTrue(results[1] is DataResource.Error)
    }

    private fun createMockEntity(id: Int, name: String, lat: Double, lng: Double): ToiletDataModel {
        return mockk<ToiletDataModel>(relaxed = true).apply {
            every { this@apply.id } returns id
            every { toiletName } returns name
            every { latitude } returns lat
            every { longitude } returns lng
            every { toDomain() } returns createDummyToilet(id, name, lat, lng)
        }
    }

    private fun createDummyToilet(id: Int, name: String, lat: Double, lng: Double) = Toilet(
        id = id,
        toiletName = name,
        roadAddress = null,
        lotAddress = null,
        isUnisex = false,
        maleToiletBowlCount = 0,
        maleUrinalCount = 0,
        maleDisabledToiletCount = 0,
        maleDisabledUrinalCount = 0,
        maleChildToiletCount = 0,
        maleChildUrinalCount = 0,
        femaleToiletBowlCount = 0,
        femaleDisabledToiletCount = 0,
        femaleChildToiletCount = 0,
        managingAgency = null,
        phoneNumber = null,
        openTime = null,
        latitude = lat,
        longitude = lng,
        emergencyBellExists = false,
        cctvExists = false,
        diaperChangingStationExists = false,
        updateDate = null
    )
}
