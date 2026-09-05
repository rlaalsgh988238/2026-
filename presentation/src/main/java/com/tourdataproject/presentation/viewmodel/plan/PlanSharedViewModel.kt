package com.tourdataproject.presentation.viewmodel.plan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.collectDataResource
import com.tourdataproject.domain.usecase.course.GetCourseByIdUseCase
import com.tourdataproject.domain.usecase.plan.AddScheduleToDayUseCase
import com.tourdataproject.domain.usecase.plan.CalculateCourseDatesUseCase
import com.tourdataproject.domain.usecase.plan.DeleteScheduleUseCase
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.domain.usecase.plan.ReorderSchedulesUseCase
import com.tourdataproject.presentation.mapper.toUiModel
import com.tourdataproject.presentation.model.KakaoMapUiModel
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.model.course.TravelCoursePresentationModel
import com.tourdataproject.presentation.utility.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlanSharedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getRegionPositionUseCase: GetRegionPositionUseCase,
    private val getCourseByIdUseCase: GetCourseByIdUseCase,
    private val calculateCourseDatesUseCase: CalculateCourseDatesUseCase,
    private val addScheduleToDayUseCase: AddScheduleToDayUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val reorderSchedulesUseCase: ReorderSchedulesUseCase
) : ViewModel() {
    private val TAG = "PlanSharedViewModel"

    private val _sharedState = MutableStateFlow(PlanSharedState())
    val sharedState = _sharedState.asStateFlow()

    init {
        // 뷰모델이 생성되는 시점에 인자 확인
        val courseId: String? = savedStateHandle["courseId"]

        if (courseId != null) {
            loadCourseById(courseId)
        }
    }

    fun onIntent(intent: PlanSharedIntent) {
        when (intent) {
            is PlanSharedIntent.OnCitySelected -> handleCitySelected(intent.cityName)
            is PlanSharedIntent.OnCityDeselected -> handleCityDeselected()
            is PlanSharedIntent.OnGetCityPosition -> fetchRegionPosition(intent.cityName)
            is PlanSharedIntent.OnDateSelected -> handleDateSelected(intent.startDate, intent.endDate)
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
        }
    }

    // TODO 이거 혹시 로드가 완전히 다 안되는거...? 왜 이렇게 됐는지 모르겠음
    private fun loadCourseById(courseId: String) {
        viewModelScope.launch {
            getCourseByIdUseCase(courseId).collectDataResource(
                onSuccess = { domainCourse ->
                    if (domainCourse != null) {
                        val uiModel = domainCourse.toUiModel()
                        _sharedState.update { it.copy(course = uiModel) }
                        if (uiModel.destination.isNotEmpty()) {
                            fetchRegionPosition(uiModel.destination)
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
            val newCourseId = if (current.course.courseId.isBlank()) {
                UUID.randomUUID().toString()
            } else {
                current.course.courseId
            }

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
            current.copy(
                course = current.course.copy(
                    destination = "",
                    courseName = ""
                )
            )
        }
    }

    private fun handleDateSelected(startDate: LocalDate, endDate: LocalDate) {
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
                )
            )
        }
    }

    private fun updateCourseName(newName: String) {
        _sharedState.update { it.copy(course = it.course.copy(courseName = newName)) }
        Log.d(TAG, "${newName}으로 수정")
    }

    private fun addScheduleToDay(targetDay: Int, newPlace: ScheduleItemUiModel) {
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

    private fun reorderSchedules(targetDay: Int, reorderedSchedules: List<ScheduleItemUiModel>) {
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

    private fun setDraftSchedule(place: KakaoMapUiModel) {
        val draft = ScheduleItemUiModel(
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

    private fun confirmAndAddSchedule(memoInput: String, accessibilityInfo: AccessibilityInfoUiModel?) {
        val currentState = _sharedState.value
        val draft = currentState.draftSchedule ?: return
        val finalSchedule = draft.copy(
            memo = memoInput,
            accessibilityInfo = accessibilityInfo ?: AccessibilityInfoUiModel()
        )
        addScheduleToDay(currentState.currentAddingDayNumber, finalSchedule)
        clearDraftSchedule()
    }

    private fun clearDraftSchedule() {
        _sharedState.update { it.copy(draftSchedule = null) }
    }

    private fun clearState(){
        _sharedState.update {
            PlanSharedState()
        }
    }
}

data class PlanSharedState(
    val course: TravelCoursePresentationModel = TravelCoursePresentationModel(),
    val currentAddingDayNumber: Int = 1,
    val draftSchedule: ScheduleItemUiModel? = null,
    val isLoading: Boolean = false
)

sealed interface PlanSharedIntent {
    data class OnCourseNameChanged(val newName: String) : PlanSharedIntent
    data class OnAddScheduleToDay(val targetDay: Int, val newPlace: ScheduleItemUiModel) : PlanSharedIntent
    data class OnDeleteSchedule(val targetDay: Int, val scheduleIdToRemove: String) : PlanSharedIntent
    data class OnReorderSchedules(val targetDay: Int, val reorderedSchedules: List<ScheduleItemUiModel>) : PlanSharedIntent
    data class OnSetAddingDayNumber(val dayNumber: Int) : PlanSharedIntent
    data class OnSetDraftSchedule(val place: KakaoMapUiModel) : PlanSharedIntent
    data class OnConfirmAndAddSchedule(val memoInput: String, val accessibilityInfo: AccessibilityInfoUiModel?) : PlanSharedIntent
    data class OnCitySelected(val cityName: String) : PlanSharedIntent
    data class OnGetCityPosition(val cityName: String): PlanSharedIntent
    object OnCityDeselected : PlanSharedIntent
    data class OnDateSelected(val startDate: LocalDate, val endDate: LocalDate) : PlanSharedIntent
    data class OnLoadCourseById(val courseId: String) : PlanSharedIntent
    object OnClearDraftSchedule : PlanSharedIntent
    object ClearPlanState : PlanSharedIntent
}
