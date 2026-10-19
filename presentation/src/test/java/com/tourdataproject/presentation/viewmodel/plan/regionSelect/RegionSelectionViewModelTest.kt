package com.tourdataproject.presentation.viewmodel.plan.regionSelect

import androidx.lifecycle.ViewModelStore
import com.braveberry.data_resource.DataResource
import com.tourdataproject.domain.model.Location
import com.tourdataproject.domain.model.Region
import com.tourdataproject.domain.usecase.plan.GetPopularCitiesUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionByKeywordUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.presentation.testing.FakeMapRepository
import com.tourdataproject.presentation.testing.MainDispatcherRule
import com.tourdataproject.presentation.testing.PresentationLogRule
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionIntent
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegionSelectionViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @get:Rule val logRule = PresentationLogRule()

    private lateinit var repository: FakeMapRepository
    private lateinit var viewModel: RegionSelectionViewModel
    private lateinit var store: ViewModelStore

    @Before
    fun setUp() {
        repository = FakeMapRepository()
        store = ViewModelStore()
        viewModel = RegionSelectionViewModel(
            GetPopularCitiesUseCase(repository),
            GetRegionByKeywordUseCase(repository),
            GetRegionPositionUseCase(repository)
        )
        store.put("subject", viewModel)
    }

    @After
    fun tearDown() {
        // 검색 Flow 수집 등 장기 실행 작업이 다음 테스트로 남지 않게 합니다.
        store.clear()
    }

    @Test
    fun `연속 입력에서는 마지막 입력 후 300ms가 지나야 마지막 검색어만 조회한다`() = runTest {
        repository.search = { flowOf(DataResource.Success(listOf(region("서울")))) }
        runCurrent()

        viewModel.onIntent(RegionSelectionIntent.OnSearchQueryChanged("서"))
        runCurrent()
        advanceTimeBy(200)
        viewModel.onIntent(RegionSelectionIntent.OnSearchQueryChanged("서울"))
        runCurrent()

        advanceTimeBy(299)
        runCurrent()
        assertTrue(repository.searchedKeywords.isEmpty())
        assertTrue(viewModel.state.value.isSearching)

        advanceTimeBy(1)
        // advanceTimeBy는 목표 시각에 예약된 작업을 실행하지 않으므로 별도로 실행합니다.
        runCurrent()
        assertEquals(listOf("서울"), repository.searchedKeywords)
        assertEquals(listOf("서울"), viewModel.state.value.searchResults.map { it.province })
        assertFalse(viewModel.state.value.isSearching)
    }

    @Test
    fun `새 검색어가 들어오면 이전 조회를 취소하고 새 결과만 표시한다`() = runTest {
        var oldSearchCancelled = false
        repository.search = { keyword ->
            if (keyword == "서울") {
                flow {
                    emit(DataResource.Loading<List<Region>>())
                    try {
                        awaitCancellation()
                    } finally {
                        oldSearchCancelled = true
                    }
                }
            } else {
                flowOf(DataResource.Success(listOf(region("부산"))))
            }
        }
        runCurrent()
        viewModel.onIntent(RegionSelectionIntent.OnSearchQueryChanged("서울"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        assertEquals(listOf("서울"), repository.searchedKeywords)

        viewModel.onIntent(RegionSelectionIntent.OnSearchQueryChanged("부산"))
        runCurrent()
        assertTrue(oldSearchCancelled)
        assertNull(viewModel.state.value.errorMessage)

        advanceTimeBy(300)
        runCurrent()
        assertEquals(listOf("서울", "부산"), repository.searchedKeywords)
        assertEquals(listOf("부산"), viewModel.state.value.searchResults.map { it.province })
        assertFalse(viewModel.state.value.isSearching)
    }

    @Test
    fun `검색 실패 후 새 검색을 하면 오류를 해제하고 성공 결과를 표시한다`() = runTest {
        repository.search = { flowOf(DataResource.Error(IOException("offline"))) }
        runCurrent()
        viewModel.onIntent(RegionSelectionIntent.OnSearchQueryChanged("서울"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        assertFalse(viewModel.state.value.isSearching)
        assertEquals("도시 검색에 실패했습니다.", viewModel.state.value.errorMessage)
        assertEquals("서울", viewModel.state.value.searchQuery)
        assertTrue(viewModel.state.value.searchResults.isEmpty())

        repository.search = { flowOf(DataResource.Success(listOf(region("부산")))) }
        viewModel.onIntent(RegionSelectionIntent.OnSearchQueryChanged("부산"))
        assertNull(viewModel.state.value.errorMessage)
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        assertEquals(listOf("부산"), viewModel.state.value.searchResults.map { it.province })
        assertFalse(viewModel.state.value.isSearching)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `Repository Flow가 예외를 던져도 검색 로딩이 종료되고 오류 상태가 된다`() = runTest {
        repository.search = { flow { throw IOException("connection interrupted") } }
        runCurrent()
        viewModel.onIntent(RegionSelectionIntent.OnSearchQueryChanged("서울"))
        runCurrent()
        advanceTimeBy(300)
        runCurrent()

        assertFalse(viewModel.state.value.isSearching)
        assertEquals("도시 검색에 실패했습니다.", viewModel.state.value.errorMessage)
        assertEquals("서울", viewModel.state.value.searchQuery)
    }

    @Test
    fun `도시 반영 완료 전 연속 클릭은 좌표 조회와 반영 요청을 중복하지 않는다`() = runTest {
        repository.position = { flowOf(DataResource.Success(Location(37.5, 127.0))) }
        val effects = mutableListOf<RegionSelectionEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects += it }
        }
        runCurrent()
        viewModel.onIntent(RegionSelectionIntent.OnInitializeEdit("course-1", "서울"))

        viewModel.onIntent(RegionSelectionIntent.OnEditNextClicked)
        viewModel.onIntent(RegionSelectionIntent.OnEditNextClicked)
        assertTrue(viewModel.state.value.isSubmitting)
        runCurrent()
        // 조회가 끝났어도 공유 상태 반영 확인 전에는 중복 제출을 막아야 합니다.
        viewModel.onIntent(RegionSelectionIntent.OnEditNextClicked)
        runCurrent()

        assertEquals(listOf("서울"), repository.requestedPositions)
        assertEquals(
            listOf(RegionSelectionEffect.ApplyEditedDestination("course-1", "서울", 37.5, 127.0)),
            effects
        )
        assertTrue(viewModel.state.value.isSubmitting)

        viewModel.onIntent(RegionSelectionIntent.OnEditApplyCompleted(true))
        assertFalse(viewModel.state.value.isSubmitting)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `도시 좌표 조회 실패 시 선택을 유지하고 반영 요청 없이 재시도를 허용한다`() = runTest {
        repository.position = { flowOf(DataResource.Error(IOException("offline"))) }
        val effects = mutableListOf<RegionSelectionEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects += it }
        }
        runCurrent()
        viewModel.onIntent(RegionSelectionIntent.OnInitializeEdit("course-1", "서울"))
        viewModel.onIntent(RegionSelectionIntent.OnEditCitySelected("부산"))
        viewModel.onIntent(RegionSelectionIntent.OnEditNextClicked)
        runCurrent()

        assertFalse(viewModel.state.value.isSubmitting)
        assertEquals("부산", viewModel.state.value.editSelectedCity)
        assertEquals("도시 위치를 확인하지 못했습니다. 다시 시도해주세요.", viewModel.state.value.errorMessage)
        assertTrue(effects.isEmpty())

        repository.position = { flowOf(DataResource.Success(Location(35.1, 129.0))) }
        viewModel.onIntent(RegionSelectionIntent.OnEditNextClicked)
        runCurrent()
        assertEquals(listOf("부산", "부산"), repository.requestedPositions)
        assertEquals(
            listOf(RegionSelectionEffect.ApplyEditedDestination("course-1", "부산", 35.1, 129.0)),
            effects
        )
        assertNull(viewModel.state.value.errorMessage)
    }

    private fun region(name: String) = Region(
        code = name,
        province = name,
        city = null,
        town = null,
        village = null
    )
}
