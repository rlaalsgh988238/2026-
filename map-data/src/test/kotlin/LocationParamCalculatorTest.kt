import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import com.tourdataproject.map_data.model.internal.CalculateLocationParams
import com.tourdataproject.map_data.uitlity.calculateLocationParams
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationParamsCalculatorTest {

    private lateinit var localDataSource: LocationLocalDataSource

    @Before
    fun setUp() {
        localDataSource = mockk()
    }

    @Test
    fun `위경도가 모두 주어지면 getUserLocation을 호출하지 않고 입력값을 반환한다`() = runTest {
        val providedLng = 127.0
        val providedLat = 37.0
        val providedRadius = 1000

        val result = localDataSource.calculateLocationParams(providedLng, providedLat, providedRadius)
        //입력값 확인
        assertEquals(CalculateLocationParams(127.0, 37.0, 1000), result)
        // 2. 로컬 GPS 조회 함수가 절대 호출되지 않았어야 함 ->굳이?
        coVerify(exactly = 0) { localDataSource.getUserLocation() }
    }

    @Test
    fun `위경도 중 하나라도 없으면 getUserLocation을 호출하고 반경이 없으면 2000을 세팅한다`() = runTest {
        coEvery { localDataSource.getUserLocation() } returns Pair(128.0, 38.0)

        val result = localDataSource.calculateLocationParams(null, null, null)

        // then
        // 1. 로컬호출값 확인
        assertEquals(CalculateLocationParams(128.0, 38.0, 2000), result)
        coVerify(exactly = 1) { localDataSource.getUserLocation() }
    }

    @Test
    fun `위경도가 없고 getUserLocation도 null을 반환하면 전부 null을 반환한다`() = runTest {
        coEvery { localDataSource.getUserLocation() } returns null

        val result = localDataSource.calculateLocationParams(null, null, null)

        //null인경우 확인 
        assertEquals(CalculateLocationParams(null, null, null), result)
        // 2. 로컬 GPS 조회 함수가 호출되었는지 확인
        coVerify(exactly = 1) { localDataSource.getUserLocation() }
    }
}