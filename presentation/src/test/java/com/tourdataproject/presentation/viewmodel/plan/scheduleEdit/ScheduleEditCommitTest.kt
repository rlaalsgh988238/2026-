package com.tourdataproject.presentation.viewmodel.plan.scheduleEdit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.tourdataproject.domain.usecase.plan.ReorderSchedulesUseCase
import com.tourdataproject.domain.usecase.plan.backUp.GetRestoredPlanStateUseCase
import com.tourdataproject.domain.usecase.plan.backUp.SavePlanStateBackupUseCase
import com.tourdataproject.presentation.model.plan.DayPlanPresentationModel
import com.tourdataproject.presentation.model.plan.ScheduleItemPresentationModel
import com.tourdataproject.presentation.testing.MainDispatcherRule
import com.tourdataproject.presentation.testing.PresentationLogRule
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedState
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEffect
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditIntent
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** 실제 두 ViewModel을 결과/응답 계약으로 연결합니다. Compose 생명주기 테스트는 아닙니다. */
@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleEditCommitTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @get:Rule val logRule = PresentationLogRule()
    private lateinit var store: ViewModelStore
    private lateinit var shared: PlanSharedViewModel
    private lateinit var editor: ScheduleEditViewModel
    private val effects = mutableListOf<ScheduleEditEffect>()
    private val observed = mutableListOf<PlanSharedState>()
    private val hotel = place("hotel")
    private val original = PlanSharedState().let { state ->
        state.copy(course = state.course.copy(dayPlans = listOf(
            DayPlanPresentationModel(rawDayNumber = 1, schedules = listOf(place("other")), stay = hotel),
            DayPlanPresentationModel(rawDayNumber = 2, schedules = listOf(place("A", 1), place("B", 2), place("C", 3)), stay = hotel),
            DayPlanPresentationModel(rawDayNumber = 3, stay = place("other-hotel"))
        )))
    }

    @Before fun setUp() {
        val restore = mockk<GetRestoredPlanStateUseCase>()
        val backup = mockk<SavePlanStateBackupUseCase>()
        every { restore() } returns emptyFlow()
        every { backup(any()) } returns emptyFlow()
        shared = PlanSharedViewModel(
            savedStateHandle = SavedStateHandle(),
            getRegionPositionUseCase = mockk(), getCourseByIdUseCase = mockk(),
            calculateCourseDatesUseCase = mockk(), addScheduleToDayUseCase = mockk(),
            deleteScheduleUseCase = mockk(), reorderSchedulesUseCase = ReorderSchedulesUseCase(),
            saveCourseUseCase = mockk(), getRestoredPlanStateUseCase = restore,
            savePlanStateBackupUseCase = backup, clearPlanStateBackupUseCase = mockk(),
            getContentIdUseCase = mockk(), getTourDataToiletInfoUseCase = mockk()
        )
        editor = ScheduleEditViewModel(original.course.dayPlans[1])
        store = ViewModelStore().also { it.put("shared", shared); it.put("editor", editor) }
        shared.onIntent(PlanSharedIntent.OnStoreBackUp(original))
    }

    @After fun tearDown() { store.clear() }

    private fun TestScope.connect() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            shared.sharedState.collect { observed += it }
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            editor.effect.collect { effect ->
                effects += effect
                if (effect is ScheduleEditEffect.SaveRequested) {
                    val accepted = shared.commitScheduleEdit(effect.result)
                    editor.onIntent(ScheduleEditIntent.OnSaveCompleted(accepted))
                }
            }
        }
        runCurrent()
    }

    private fun editAndDeleteStay() {
        editor.onIntent(ScheduleEditIntent.OnScheduleMoved(0, 2))
        editor.onIntent(ScheduleEditIntent.OnScheduleMoveFinished)
        editor.onIntent(ScheduleEditIntent.OnStayDeleted)
    }

    @Test fun `순서 변경 후 숙소를 삭제해도 초안 순서는 유지되고 공유 상태와 Effect는 변경되지 않는다`() = runTest {
        connect()
        editAndDeleteStay()
        runCurrent()
        assertEquals(listOf("B", "C", "A"), editor.state.value.schedules.map { it.scheduleId })
        assertNull(editor.state.value.stay)
        assertEquals(original, shared.sharedState.value)
        assertTrue(effects.isEmpty())
    }

    @Test fun `저장하면 순서와 숙소 삭제가 하나의 공유 상태 변경으로 반영된다`() = runTest {
        connect()
        editAndDeleteStay()
        editor.onIntent(ScheduleEditIntent.OnStayDeleted) // 중복 입력에도 삭제 정보 보존
        editor.onIntent(ScheduleEditIntent.OnSaveClicked)
        runCurrent()
        val result = shared.sharedState.value
        val days = result.course.dayPlans
        assertEquals(listOf("B", "C", "A"), days[1].schedules.map { it.scheduleId })
        assertEquals(listOf(1, 2, 3), days[1].schedules.map { it.order })
        assertEquals(original.course.dayPlans[0].schedules, days[0].schedules)
        assertTrue(days[0].stay.scheduleId.isBlank()) // 기존 다중 날짜 삭제 정책
        assertTrue(days[1].stay.scheduleId.isBlank())
        assertEquals(original.course.dayPlans[2], days[2])
        assertEquals(listOf(original, result), observed)
        assertEquals(1, effects.filterIsInstance<ScheduleEditEffect.SaveRequested>().size)
        assertEquals(ScheduleEditEffect.NavigateBack, effects.last())
    }

    @Test fun `숙소 삭제 후 취소하고 다시 진입하면 원래 일정과 숙소가 유지된다`() = runTest {
        connect()
        editAndDeleteStay()
        editor.onIntent(ScheduleEditIntent.OnBackClicked)
        runCurrent()
        assertEquals(original, shared.sharedState.value)
        assertEquals(listOf(ScheduleEditEffect.NavigateBack), effects)
        val reopened = ScheduleEditViewModel(shared.sharedState.value.course.dayPlans[1])
        store.put("reopened", reopened)
        assertEquals(listOf("A", "B", "C"), reopened.state.value.schedules.map { it.scheduleId })
        assertEquals(hotel, reopened.state.value.stay)
    }

    @Test fun `숙소를 삭제하지 않고 일정만 저장하면 모든 숙소는 유지된다`() = runTest {
        connect()
        editor.onIntent(ScheduleEditIntent.OnScheduleDeleted("B"))
        editor.onIntent(ScheduleEditIntent.OnSaveClicked)
        runCurrent()
        val days = shared.sharedState.value.course.dayPlans
        assertEquals(listOf("A", "C"), days[1].schedules.map { it.scheduleId })
        assertEquals(original.course.dayPlans.map { it.stay }, days.map { it.stay })
    }

    @Test fun `공유 계획이 갱신되어도 진행 중인 편집 초안으로 흘러들지 않는다`() = runTest {
        connect()
        editAndDeleteStay()
        val draft = editor.state.value
        val changed = original.copy(course = original.course.copy(
            courseName = "새 여행 이름",
            dayPlans = original.course.dayPlans.map { day ->
                if (day.rawDayNumber == 1) day.copy(schedules = emptyList()) else day
            }
        ))
        shared.onIntent(PlanSharedIntent.OnStoreBackUp(changed))
        runCurrent()
        assertEquals(draft, editor.state.value)
        editor.onIntent(ScheduleEditIntent.OnSaveClicked)
        runCurrent()
        assertEquals("새 여행 이름", shared.sharedState.value.course.courseName)
        assertTrue(shared.sharedState.value.course.dayPlans[0].schedules.isEmpty())
    }

    @Test fun `대상 날짜가 사라지면 저장을 거절하고 다른 날짜의 숙소를 삭제하지 않는다`() = runTest {
        connect()
        editAndDeleteStay()
        val changed = original.copy(course = original.course.copy(
            dayPlans = original.course.dayPlans.filterNot { it.rawDayNumber == 2 }
        ))
        shared.onIntent(PlanSharedIntent.OnStoreBackUp(changed))
        editor.onIntent(ScheduleEditIntent.OnSaveClicked)
        runCurrent()
        assertEquals(changed, shared.sharedState.value)
        assertFalse(editor.state.value.isLoading)
        assertFalse(effects.contains(ScheduleEditEffect.NavigateBack))
    }

    private fun place(id: String, order: Int = 0) = ScheduleItemPresentationModel(
        scheduleId = id, scheduleName = id, order = order, memo = "memo-$id"
    )
}
