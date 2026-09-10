package com.tourdataproject.presentation.viewmodel.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.collectDataResource
import com.tourdataproject.domain.usecase.course.GetCourseByIdUseCase
import com.tourdataproject.domain.usecase.course.SaveCourseUseCase
import com.tourdataproject.domain.usecase.plan.AddScheduleToDayUseCase
import com.tourdataproject.domain.usecase.plan.CalculateCourseDatesUseCase
import com.tourdataproject.domain.usecase.plan.backUp.ClearPlanStateBackupUseCase
import com.tourdataproject.domain.usecase.plan.DeleteScheduleUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.domain.usecase.plan.backUp.GetRestoredPlanStateUseCase
import com.tourdataproject.domain.usecase.plan.ReorderSchedulesUseCase
import com.tourdataproject.domain.usecase.plan.backUp.SavePlanStateBackupUseCase
import com.tourdataproject.presentation.mapper.toUiModel
import com.tourdataproject.presentation.model.KakaoMapPresentationModel
import com.tourdataproject.presentation.model.plan.AccessibilityInfoPresentationModel
import com.tourdataproject.presentation.model.plan.ScheduleItemPresentationModel
import com.tourdataproject.presentation.utility.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@HiltViewModel
class PlanSharedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getRegionPositionUseCase: GetRegionPositionUseCase,
    private val getCourseByIdUseCase: GetCourseByIdUseCase,
    private val calculateCourseDatesUseCase: CalculateCourseDatesUseCase,
    private val addScheduleToDayUseCase: AddScheduleToDayUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val reorderSchedulesUseCase: ReorderSchedulesUseCase,
    private val saveCourseUseCase: SaveCourseUseCase,
    private val getRestoredPlanStateUseCase: GetRestoredPlanStateUseCase,
    private val savePlanStateBackupUseCase: SavePlanStateBackupUseCase,
    private val clearPlanStateBackupUseCase: ClearPlanStateBackupUseCase
) : ViewModel() {

    private val TAG = "PlanSharedViewModel"

    private val _sharedState = MutableStateFlow(PlanSharedState())
    val sharedState = _sharedState.asStateFlow()

    private val _effect = MutableSharedFlow<PlanSharedEffect>()
    val effect: SharedFlow<PlanSharedEffect> = _effect.asSharedFlow()

    init {
        backUpData()
        initializePlanState()
        observeAndBackupState()
    }

    fun onIntent(intent: PlanSharedIntent) {
        when (intent) {
            is PlanSharedIntent.OnCitySelected -> handleCitySelected(intent.cityName)
            is PlanSharedIntent.OnCityDeselected -> handleCityDeselected()
            is PlanSharedIntent.OnGetCityPosition -> fetchRegionPosition(intent.cityName)
            is PlanSharedIntent.OnCourseNameChanged -> updateCourseName(intent.newName)
            is PlanSharedIntent.OnAddScheduleToDay -> addScheduleToDay(intent.targetDay, intent.newPlace)
            is PlanSharedIntent.OnDeleteSchedule -> deleteSchedule(intent.targetDay, intent.scheduleIdToRemove)
            is PlanSharedIntent.OnReorderSchedules -> reorderSchedules(intent.targetDay, intent.reorderedSchedules)
            is PlanSharedIntent.OnSetAddingDayNumber -> updateAddingDayNumber(intent.dayNumber)
            is PlanSharedIntent.OnSetDraftSchedule -> setDraftSchedule(intent.place)
            is PlanSharedIntent.OnConfirmAndAddSchedule -> confirmAndAddSchedule(intent.memoInput, intent.accessibilityInfo)
            is PlanSharedIntent.OnLoadCourseById -> loadCourseById(intent.courseId)
            is PlanSharedIntent.OnClearDraftSchedule -> clearDraftSchedule()
            is PlanSharedIntent.ClearPlanState -> clearState()
            is PlanSharedIntent.OnCalendarDateTapped -> handleCalendarDateTapped(intent.date)
            is PlanSharedIntent.OnConfirmDateSelection -> confirmDateSelection()
            is PlanSharedIntent.OnSaveCourse -> saveCourse()
            is PlanSharedIntent.OnStoreBackUp -> storeBackUp(intent.state)
            is PlanSharedIntent.OnClearBackUp -> clearBackUp()
            is PlanSharedIntent.OnSetDraftStay -> setDraftStay(intent.place)
            is PlanSharedIntent.OnConfirmStaySelection -> confirmStaySelection()
            is PlanSharedIntent.OnClearDraftStay -> clearDraftStay()
            is PlanSharedIntent.OnDeleteStay -> deleteStay(intent.scheduleId)
        }
    }

    private fun deleteStay(scheduleId: String) {
        _sharedState.update { currentState ->
            val updatedDayPlans = currentState.course.dayPlans.map { dayPlan ->
                if (dayPlan.stay.scheduleId == scheduleId) {
                    dayPlan.copy(stay = ScheduleItemPresentationModel())
                } else dayPlan
            }
            currentState.copy(course = currentState.course.copy(dayPlans = updatedDayPlans))
        }
        Log.d(TAG, "숙소 삭제: $scheduleId")
    }


    private fun initializePlanState() {
        val requestedCourseId: String? = savedStateHandle["courseId"]
        if (requestedCourseId != null) {
            onIntent(PlanSharedIntent.OnLoadCourseById(requestedCourseId))
        }
    }

    private fun backUpData(){
        viewModelScope.launch {
            getRestoredPlanStateUseCase().collectDataResource(
                onSuccess = { backup ->
                    if (backup != null && backup != PlanSharedState()) {
                        val restoredState = PlanSharedState(
                            course = backup.course.toUiModel(),
                            currentAddingDayNumber = backup.currentAddingDayNumber,
                            draftStartDate = backup.draftStartDate,
                            draftEndDate = backup.draftEndDate
                        )
                        onIntent(PlanSharedIntent.OnStoreBackUp(restoredState))
                        Log.d(TAG, "프로세스 데스 복구 완료")
                    } else{
                        Log.d(TAG, "백업데이터 없음")
                    }
                },
                onError = { Log.e(TAG, "백업 확인 실패: ${it.message}") }
            )
        }
    }

    private fun clearBackUp(){
        viewModelScope.launch {
            clearPlanStateBackupUseCase().collectDataResource(
                onSuccess = {Log.d(TAG, "백업 데이터 삭제 성공")},
                onError = {Log.d(TAG, "백업 데이터 삭제 실패")}
            )
        }
    }

    private fun storeBackUp(state: PlanSharedState){
        _sharedState.value = state
    }

    private fun observeAndBackupState() {
        viewModelScope.launch {
            _sharedState
                .drop(1)
                .debounce(500L.milliseconds)
                .collectLatest { state ->
                    val backup = state.toBackUp()
                    savePlanStateBackupUseCase(backup).collectDataResource(
                        onSuccess = { Log.d(TAG, "자동 백업 완료") },
                        onError = { Log.e(TAG, "자동 백업 실패: ${it.message}") }
                    )
                }
        }
    }

    private fun saveCourse() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🚨 코스저장 시도")
                val currentCourse = _sharedState.value.course
                saveCourseUseCase(currentCourse.toDomain())
                clearPlanStateBackupUseCase().collectDataResource(
                    onSuccess = { Log.d(TAG, "백업 데이터 초기화 완료") },
                    onError = { Log.e(TAG, "백업 초기화 실패") }
                )
                _effect.emit(PlanSharedEffect.NavigateToHomeScreen)
            } catch (e: Exception) {
                _effect.emit(PlanSharedEffect.ShowToast("코스 저장에 실패했습니다."))
                Log.e(TAG, "코스 저장 에러: ${e.message}")
            }
        }
    }

    private fun loadCourseById(courseId: String) {
        viewModelScope.launch {
            getCourseByIdUseCase(courseId).collectDataResource(
                onSuccess = { domainCourse ->
                    if (domainCourse != null) {
                        val uiModel = domainCourse.toUiModel()
                        _sharedState.update { it.copy(course = uiModel) }
                        if (uiModel.destination.isNotEmpty()) {
                            onIntent(PlanSharedIntent.OnGetCityPosition(uiModel.destination))
                        }
                    }
                },
                onError = { Log.e(TAG, "코스 불러오기 에러: ${it.message}") }
            )
        }
    }

    private fun fetchRegionPosition(cityName: String) {
        viewModelScope.launch {
            getRegionPositionUseCase(cityName).collectDataResource(
                onSuccess = { location ->
                    _sharedState.update {
                        it.copy(
                            course = it.course.copy(
                                destinationLatitude = location.latitude,
                                destinationLongitude = location.longitude
                            )
                        )
                    }
                    Log.d(TAG, "좌표: ${location.latitude},${location.longitude}")
                },
                onError = { Log.e(TAG, "좌표 복구 에러: ${it.message}") }
            )
        }
    }

    private fun handleCitySelected(cityName: String) {
        _sharedState.update { current ->
            val newCourseId = if (current.course.courseId.isBlank()) UUID.randomUUID().toString() else current.course.courseId
            current.copy(
                course = current.course.copy(
                    courseId = newCourseId,
                    destination = cityName,
                    courseName = "${cityName} 여행"
                )
            )
        }
        Log.d(TAG, cityName)
    }

    private fun handleCityDeselected() {
        _sharedState.update { current ->
            current.copy(course = current.course.copy(destination = "", courseName = ""))
        }
    }

    private fun handleCalendarDateTapped(clickedDate: LocalDate) {
        val today = LocalDate.now(ZoneId.systemDefault())
        if (clickedDate.isBefore(today)) return

        _sharedState.update { current ->
            val start = current.draftStartDate?.toLocalDate()
            val end = current.draftEndDate?.toLocalDate()

            val (newStart, newEnd) = when {
                start == null || (start != null && end != null) -> clickedDate to null
                clickedDate.isBefore(start) -> clickedDate to null
                clickedDate == start -> null to null
                else -> start to clickedDate
            }

            Log.d(TAG, "날짜 선택됨: 시작일=${newStart ?: "미선택"}, 종료일=${newEnd ?: "미선택"}")

            current.copy(
                draftStartDate = newStart?.toEpochMillis(),
                draftEndDate = newEnd?.toEpochMillis()
            )
        }
    }

    // 코스 전체 여행 날짜 확정 (region_selection -> date_selection 흐름)
    private fun confirmDateSelection() {
        val state = _sharedState.value
        val startLong = state.draftStartDate ?: return
        val endLong = state.draftEndDate ?: return

        val startDate = startLong.toLocalDate()
        val endDate = endLong.toLocalDate()

        val result = calculateCourseDatesUseCase(startDate, endDate)

        val periodFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val datePeriodString = "${startDate.format(periodFormatter)} ~ ${endDate.format(periodFormatter)}"
        Log.d(TAG, datePeriodString)

        _sharedState.update { currentState ->
            currentState.copy(
                course = currentState.course.copy(
                    rawStartDate = result.startMillis,
                    rawEndDate = result.endMillis,
                    datePeriod = datePeriodString,
                    dayPlans = result.dayPlans.map { it.toUiModel() }
                ),
                draftStartDate = null,
                draftEndDate = null
            )
        }
    }

    private fun confirmStaySelection() {
        val currentState = _sharedState.value
        val startLong = currentState.draftStartDate ?: return
        val endLong = currentState.draftEndDate ?: return
        val stay = currentState.draftStay ?: return

        val checkInDate = startLong.toLocalDate()
        val checkOutDate = endLong.toLocalDate()

        // 체크아웃 당일은 숙박하는 날이 아니므로 checkInDate ~ checkOutDate 전날까지가 숙박일
        val stayNights = generateSequence(checkInDate) { it.plusDays(1) }
            .takeWhile { it.isBefore(checkOutDate) }
            .toList()

        Log.d(TAG, "숙소 확정: ${stay.scheduleName}, 체크인=${checkInDate}, 체크아웃=${checkOutDate}")

        _sharedState.update { state ->
            val updatedDayPlans = state.course.dayPlans.map { dayPlan ->
                val dayDate = dayPlan.rawDate.toLocalDate()
                if (dayDate in stayNights) {
                    dayPlan.copy(stay = stay)
                } else {
                    dayPlan
                }
            }
            state.copy(
                course = state.course.copy(dayPlans = updatedDayPlans),
                draftStay = null,
                draftStartDate = null,
                draftEndDate = null
            )
        }
    }

    private fun updateCourseName(newName: String) {
        _sharedState.update { it.copy(course = it.course.copy(courseName = newName)) }
        Log.d(TAG, "${newName}으로 수정")
    }

    private fun addScheduleToDay(targetDay: Int, newPlace: ScheduleItemPresentationModel) {
        _sharedState.update { currentState ->
            val currentDomainPlans = currentState.course.dayPlans.map { it.toDomain() }
            val updatedDomainPlans = addScheduleToDayUseCase(currentDomainPlans, targetDay, newPlace.toDomain())
            currentState.copy(course = currentState.course.copy(dayPlans = updatedDomainPlans.map { it.toUiModel() }))
        }
        Log.d(TAG, "${newPlace.scheduleId} 추가")
    }

    private fun deleteSchedule(targetDay: Int, scheduleIdToRemove: String) {
        _sharedState.update { currentState ->
            val currentDomainPlans = currentState.course.dayPlans.map { it.toDomain() }
            val updatedDomainPlans = deleteScheduleUseCase(currentDomainPlans, targetDay, scheduleIdToRemove)
            currentState.copy(course = currentState.course.copy(dayPlans = updatedDomainPlans.map { it.toUiModel() }))
        }
        Log.d(TAG, "${scheduleIdToRemove} 삭제")
    }

    private fun reorderSchedules(targetDay: Int, reorderedSchedules: List<ScheduleItemPresentationModel>) {
        _sharedState.update { currentState ->
            val currentDomainPlans = currentState.course.dayPlans.map { it.toDomain() }
            val domainReordered = reorderedSchedules.map { it.toDomain() }
            val updatedDomainPlans = reorderSchedulesUseCase(currentDomainPlans, targetDay, domainReordered)
            currentState.copy(course = currentState.course.copy(dayPlans = updatedDomainPlans.map { it.toUiModel() }))
        }
        Log.d(TAG, "스케줄 재정렬")
    }

    private fun updateAddingDayNumber(dayNumber: Int) {
        _sharedState.update { it.copy(currentAddingDayNumber = dayNumber) }
    }

    private fun setDraftSchedule(place: KakaoMapPresentationModel) {
        val draft = ScheduleItemPresentationModel(
            scheduleId = UUID.randomUUID().toString(),
            scheduleName = place.placeName,
            latitude = place.y,
            longitude = place.x,
            placeId = place.id,
            address = place.address,
            category = place.category,
            memo = ""
        )
        _sharedState.update { it.copy(draftSchedule = draft) }
    }

    private fun confirmAndAddSchedule(memoInput: String, accessibilityInfo: AccessibilityInfoPresentationModel?) {
        val currentState = _sharedState.value
        val draft = currentState.draftSchedule ?: return

        val finalSchedule = draft.copy(
            memo = memoInput,
            accessibilityInfo = accessibilityInfo ?: AccessibilityInfoPresentationModel()
        )

        addScheduleToDay(currentState.currentAddingDayNumber, finalSchedule)
        clearDraftSchedule()
    }

    private fun clearDraftSchedule() {
        _sharedState.update { it.copy(draftSchedule = null) }
    }

    private fun clearState(){
        _sharedState.update { PlanSharedState() }
    }

    private fun setDraftStay(stay: KakaoMapPresentationModel){
        val draft = ScheduleItemPresentationModel(
            scheduleId = UUID.randomUUID().toString(),
            scheduleName = stay.placeName,
            latitude = stay.y,
            longitude = stay.x,
            placeId = stay.id,
            address = stay.address,
            category = stay.category,
            memo = ""
        )
        _sharedState.update {
            it.copy(
                draftStay = draft
            )
        }
    }

    private fun clearDraftStay() {
        _sharedState.update { it.copy(draftStay = null, draftStartDate = null, draftEndDate = null) }
    }
}

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.toEpochMillis(): Long =
    this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()