package com.braveberry.toilet_data.impl

import com.braveberry.data_resource.DataResource
import com.braveberry.toilet_data.localDB.ToiletDataSource
import com.braveberry.toilet_data.model.ToiletEntity
import com.braveberry.toilet_data.utiltiy.FilterCalculator
import com.tourdataproject.domain.model.Toilet
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
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

        val result = repository.getToiletsByDistance(dist, lat, lng).first()

        assertTrue(result is DataResource.Success)
        val data = (result as DataResource.Success).data
        assertEquals(1, data.size)
        assertEquals("In", data[0].toiletName)
    }

    @Test
    fun getToiletsByDistance_returnsErrorOnFailure() = runTest {
        coEvery { dataSource.getToiletDataInBox(any(), any(), any()) } throws RuntimeException()

        val result = repository.getToiletsByDistance(1.0f, 37.0, 127.0).first()

        assertTrue(result is DataResource.Error)
    }

    private fun createMockEntity(id: Int, name: String, lat: Double, lng: Double): ToiletEntity {
        return mockk<ToiletEntity>(relaxed = true).apply {
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
