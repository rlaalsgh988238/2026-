package com.tourdataproject.presentation.viewmodel.plan.scheduleEdit

import androidx.lifecycle.ViewModelProvider
import com.tourdataproject.presentation.model.plan.DayPlanPresentationModel
import com.tourdataproject.presentation.model.plan.ScheduleEditResult
import androidx.lifecycle.ViewModelStore
import com.tourdataproject.presentation.model.plan.ScheduleItemPresentationModel
import com.tourdataproject.presentation.testing.MainDispatcherRule
import com.tourdataproject.presentation.testing.PresentationLogRule
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEffect
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleEditViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @get:Rule val logRule = PresentationLogRule()

    private lateinit var viewModel: ScheduleEditViewModel
    private lateinit var store: ViewModelStore

    private val first = place("A", 1)
    private val second = place("B", 2)
    private val third = place("C", 3)
    private val stay = place("hotel", 0)

    @Before
    fun setUp() {
        store = ViewModelStore()
        viewModel = ViewModelProvider(store, ScheduleEditViewModel.factory(initialDay()))["subject", ScheduleEditViewModel::class.java]
    }

    @After
    fun tearDown() {
        // Rule이 Main을 복원하기 전에 viewModelScope의 작업을 취소합니다.
        store.clear()
    }

    @Test
    fun `장소를 삭제하면 나머지 장소의 내용과 숙소를 유지하고 순번을 다시 매긴다`() {
        viewModel.onIntent(ScheduleEditIntent.OnScheduleDeleted("B"))

        assertEquals(listOf(first, third.copy(order = 2)), viewModel.state.value.schedules)
        assertEquals(stay, viewModel.state.value.stay)
        assertEquals(2, viewModel.state.value.dayNumber)
    }

    @Test
    fun `드래그 완료 시 이동한 순서대로 순번을 매기고 장소 데이터는 유지한다`() {
        viewModel.onIntent(ScheduleEditIntent.OnScheduleMoved(0, 2))
        viewModel.onIntent(ScheduleEditIntent.OnScheduleMoveFinished)

        assertEquals(
            listOf(second.copy(order = 1), third.copy(order = 2), first.copy(order = 3)),
            viewModel.state.value.schedules
        )
        assertEquals(stay, viewModel.state.value.stay)
    }

    @Test
    fun `범위를 벗어난 드래그 요청은 편집 상태를 변경하지 않는다`() {
        val before = viewModel.state.value

        viewModel.onIntent(ScheduleEditIntent.OnScheduleMoved(-1, 1))
        viewModel.onIntent(ScheduleEditIntent.OnScheduleMoved(0, 3))

        assertEquals(before, viewModel.state.value)
    }

    @Test
    fun `뒤로가기는 초안을 저장하라는 명령 없이 이동 Effect만 보낸다`() = runTest {
        val effects = mutableListOf<ScheduleEditEffect>()
        // replay=0인 SharedFlow이므로 입력 전에 수집을 시작합니다.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects += it }
        }
        viewModel.onIntent(ScheduleEditIntent.OnScheduleDeleted("B"))
        val draft = viewModel.state.value

        viewModel.onIntent(ScheduleEditIntent.OnBackClicked)
        runCurrent()

        assertEquals(listOf(ScheduleEditEffect.NavigateBack), effects)
        assertEquals(draft, viewModel.state.value)
    }

    @Test
    fun `저장 결과를 전달하고 확정 응답을 받은 뒤에만 안내와 뒤로가기를 보낸다`() = runTest {
        val effects = mutableListOf<ScheduleEditEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects += it }
        }
        viewModel.onIntent(ScheduleEditIntent.OnScheduleDeleted("B"))

        viewModel.onIntent(ScheduleEditIntent.OnSaveClicked)
        runCurrent()

        assertEquals(1, effects.size)
        assertTrue(viewModel.state.value.isLoading)
        assertEquals(
            ScheduleEditEffect.SaveRequested(ScheduleEditResult(2, listOf(first, third.copy(order = 2)))),
            effects[0]
        )
        viewModel.onIntent(ScheduleEditIntent.OnSaveCompleted(true))
        runCurrent()
        assertEquals(3, effects.size)
        assertEquals(ScheduleEditEffect.ShowToast("일정이 저장되었습니다."), effects[1])
        assertEquals(ScheduleEditEffect.NavigateBack, effects[2])
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `같은 화면 소유자에서 원본이 바뀌어도 기존 편집 ViewModel과 초안을 유지한다`() {
        viewModel.onIntent(ScheduleEditIntent.OnScheduleMoved(0, 2))
        viewModel.onIntent(ScheduleEditIntent.OnScheduleMoveFinished)
        assertEquals(listOf("B", "C", "A"), viewModel.state.value.schedules.map { it.scheduleId })

        viewModel.onIntent(ScheduleEditIntent.OnStayDeleted)
        val retrieved = ViewModelProvider(
            store, ScheduleEditViewModel.factory(initialDay().copy(schedules = emptyList()))
        )["subject", ScheduleEditViewModel::class.java]
        assertSame(viewModel, retrieved)

        assertEquals(listOf("B", "C", "A"), viewModel.state.value.schedules.map { it.scheduleId })
        assertEquals(null, viewModel.state.value.stay)
        assertEquals("hotel", viewModel.state.value.deletedStayId)
    }

    @Test
    fun `확정 거절 시 초안을 유지하고 뒤로가지 않으며 재시도할 수 있다`() = runTest {
        val effects = mutableListOf<ScheduleEditEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects += it }
        }
        viewModel.onIntent(ScheduleEditIntent.OnStayDeleted)
        viewModel.onIntent(ScheduleEditIntent.OnSaveClicked)
        runCurrent()
        viewModel.onIntent(ScheduleEditIntent.OnSaveCompleted(false))
        runCurrent()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("hotel", viewModel.state.value.deletedStayId)
        assertFalse(effects.contains(ScheduleEditEffect.NavigateBack))
        assertTrue(effects.last() is ScheduleEditEffect.ShowToast)
        viewModel.onIntent(ScheduleEditIntent.OnSaveClicked)
        runCurrent()
        assertEquals(2, effects.filterIsInstance<ScheduleEditEffect.SaveRequested>().size)
    }

    @Test
    fun `확정 응답 대기 중 중복 저장과 편집 입력은 제출 결과를 바꾸지 않는다`() = runTest {
        val effects = mutableListOf<ScheduleEditEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effect.collect { effects += it }
        }
        viewModel.onIntent(ScheduleEditIntent.OnSaveClicked)
        viewModel.onIntent(ScheduleEditIntent.OnSaveClicked)
        viewModel.onIntent(ScheduleEditIntent.OnScheduleDeleted("A"))
        runCurrent()
        assertEquals(1, effects.size)
        assertEquals(listOf(first, second, third), viewModel.state.value.schedules)
    }

    @Test
    fun `생성 이후 외부 원본 목록 변경은 편집 초안에 반영되지 않는다`() {
        val source = mutableListOf(first, second, third)
        val isolated = ScheduleEditViewModel(initialDay().copy(schedules = source))
        store.put("isolated", isolated)
        source.clear()
        assertEquals(listOf(first, second, third), isolated.state.value.schedules)
    }

    private fun initialDay() = DayPlanPresentationModel(
        rawDayNumber = 2,
        dateLabel = "10/17",
        schedules = listOf(first, second, third),
        stay = stay
    )

    private fun place(id: String, order: Int) = ScheduleItemPresentationModel(
        scheduleId = id,
        order = order,
        scheduleName = "장소 $id",
        memo = "보존해야 하는 메모 $id",
        latitude = 37.5,
        longitude = 127.0
    )
}
