import com.braveberry.data_resource.DataResource
import com.tourdataproject.map_data.datasource.KakaoMapRemoteDataSource
import com.tourdataproject.map_data.datasource.LocationLocalDataSource
import com.tourdataproject.map_data.datasource.RegionLocalDataSource
import com.tourdataproject.map_data.impl.MapRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapRepositoryImplTest {

    // 1. 가짜(Mock) 의존성 객체 선언
    private lateinit var remoteDataSource: KakaoMapRemoteDataSource
    private lateinit var locationLocalDataSource: LocationLocalDataSource
    private lateinit var regionLocalDataSource: RegionLocalDataSource
    private lateinit var repository: MapRepositoryImpl

    @Before
    fun setUp() {
        remoteDataSource = mockk()
        locationLocalDataSource = mockk()
        regionLocalDataSource = mockk() // 🌟 추가

        // 🌟 3개의 의존성 모두 주입하도록 수정
        repository = MapRepositoryImpl(remoteDataSource, locationLocalDataSource, regionLocalDataSource)
    }

    @Test
    fun `파라미터로 위치가 주어지면 로컬GPS를 조회하지 않고 원격 API를 호출한다`() = runTest {
         val query = "스타벅스"
        val providedLng = 127.0
        val providedLat = 37.0
        val providedRadius = 1000
        val page = 1

        // 원격 API가 정상적으로 호출되면 빈 리스트(Success)를 반환하도록 조작
        every {
            remoteDataSource.getNearbyPlaces(query, providedLng, providedLat, providedRadius, page)
        } returns flowOf(DataResource.Success(emptyList()))

        val result = repository.getNearbyPlaces(query, providedLng, providedLat, providedRadius, page).first()

        assertTrue(result is DataResource.Success)

        coVerify(exactly = 0) { locationLocalDataSource.getUserLocation() }

        // 🌟 핵심 검증 2: 내가 넘긴 파라미터 그대로 원격 API가 1번 호출되어야 함!
        verify(exactly = 1) {
            remoteDataSource.getNearbyPlaces(query, providedLng, providedLat, providedRadius, page)
        }
    }

    @Test
    fun `위치 파라미터가 없으면 로컬GPS를 조회하고 기본 반경 2000으로 원격 API를 호출한다`() = runTest {

        val query = "스타벅스"
        val localLng = 128.0
        val localLat = 38.0
        val page = 1

        coEvery { locationLocalDataSource.getUserLocation() } returns Pair(localLng, localLat)

        every {
            remoteDataSource.getNearbyPlaces(query, localLng, localLat, 2000, page)
        } returns flowOf(DataResource.Success(emptyList()))

        // when (위치와 반경을 모두 null로 넘김)
        val result = repository.getNearbyPlaces(query, null, null, null, page).first()

        // then
        assertTrue(result is DataResource.Success)

        coVerify(exactly = 1) { locationLocalDataSource.getUserLocation() }

        // 🌟 핵심 검증 2: 로컬에서 가져온 위경도 값과 기본 반경(2000)으로 원격 API가 호출되어야 함!
        verify(exactly = 1) {
            remoteDataSource.getNearbyPlaces(query, localLng, localLat, 2000, page)
        }
    }

    @Test
    fun `데이터 통신 중 예외가 발생하면 catch 블록에서 DataResource Error로 반환한다`() = runTest {
        // given
        val query = "오류테스트"
        val exception = RuntimeException("네트워크 연결 실패")

        // 원격 API 호출 시 고의로 에러(Exception)를 방출(Throw)하도록 조작
        every {
            remoteDataSource.getNearbyPlaces(any(), any(), any(), any(), any())
        } returns flow { throw exception }

        // when
        val result = repository.getNearbyPlaces(query, 127.0, 37.0, 1000, 1).first()

        // then
        // 🌟 핵심 검증: 앱이 크래시(종료)되지 않고, Error 타입으로 감싸져서 내려와야 함!
        assertTrue(result is DataResource.Error)
        assertEquals("네트워크 연결 실패", (result as DataResource.Error).throwable.message)
    }
}