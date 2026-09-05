package com.tourdataproject.presentation.viewmodel.plan

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
import com.tourdataproject.presentation.model.course.TravelCourseUiModel
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

    fun setEvent(event: PlanSharedEvent) {
        when (event) {
            is PlanSharedEvent.OnCitySelected -> handleCitySelected(event.cityName)
            is PlanSharedEvent.OnDateSelected -> handleDateSelected(event.startDate, event.endDate)
            is PlanSharedEvent.OnCourseNameChanged -> updateCourseName(event.newName)
            is PlanSharedEvent.OnAddScheduleToDay -> addScheduleToDay(event.targetDay, event.newPlace)
            is PlanSharedEvent.OnDeleteSchedule -> deleteSchedule(event.targetDay, event.scheduleIdToRemove)
            is PlanSharedEvent.OnReorderSchedules -> reorderSchedules(event.targetDay, event.reorderedSchedules)
            is PlanSharedEvent.OnSetAddingDayNumber -> updateAddingDayNumber(event.dayNumber)
            is PlanSharedEvent.OnSetDraftSchedule -> setDraftSchedule(event.place)
            is PlanSharedEvent.OnConfirmAndAddSchedule -> confirmAndAddSchedule(event.memoInput, event.accessibilityInfo)
            is PlanSharedEvent.OnLoadCourseById -> loadCourseById(event.courseId)
            is PlanSharedEvent.OnClearDraftSchedule -> clearDraftSchedule()
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
                },
                onError = { Log.e(TAG, "좌표 복구 에러: ${it.message}") }
            )
        }
    }

    private fun handleCitySelected(cityName: String) {
        _sharedState.update { current ->
            current.copy(
                course = current.course.copy(
                    courseId = UUID.randomUUID().toString(),
                    destination = cityName,
                    courseName = "${cityName} 여행"
                )
            )
        }
        fetchRegionPosition(cityName)
        Log.d(TAG, cityName)
    }

    private fun handleDateSelected(startDate: LocalDate, endDate: LocalDate) {
        val result = calculateCourseDatesUseCase(startDate, endDate)

        val periodFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val datePeriodString = "${startDate.format(periodFormatter)} ~ ${endDate.format(periodFormatter)}"

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
    }

    private fun addScheduleToDay(targetDay: Int, newPlace: ScheduleItemUiModel) {
        _sharedState.update { currentState ->
            val currentDomainPlans = currentState.course.dayPlans.map { it.toDomain() }
            val updatedDomainPlans = addScheduleToDayUseCase(currentDomainPlans, targetDay, newPlace.toDomain())

            currentState.copy(
                course = currentState.course.copy(dayPlans = updatedDomainPlans.map { it.toUiModel() })
            )
        }
    }

    private fun deleteSchedule(targetDay: Int, scheduleIdToRemove: String) {
        _sharedState.update { currentState ->
            val currentDomainPlans = currentState.course.dayPlans.map { it.toDomain() }
            val updatedDomainPlans = deleteScheduleUseCase(currentDomainPlans, targetDay, scheduleIdToRemove)

            currentState.copy(
                course = currentState.course.copy(dayPlans = updatedDomainPlans.map { it.toUiModel() })
            )
        }
    }

    private fun reorderSchedules(targetDay: Int, reorderedSchedules: List<ScheduleItemUiModel>) {
        _sharedState.update { currentState ->
            val currentDomainPlans = currentState.course.dayPlans.map { it.toDomain() }
            val domainReordered = reorderedSchedules.map { it.toDomain() }
            val updatedDomainPlans = reorderSchedulesUseCase(currentDomainPlans, targetDay, domainReordered)

            currentState.copy(
                course = currentState.course.copy(dayPlans = updatedDomainPlans.map { it.toUiModel() })
            )
        }
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

        // 기존 addScheduleToDay 로직 재활용
        addScheduleToDay(currentState.currentAddingDayNumber, finalSchedule)
        clearDraftSchedule()
    }

    private fun clearDraftSchedule() {
        _sharedState.update { it.copy(draftSchedule = null) }
    }
}

data class PlanSharedState(
    val course: TravelCourseUiModel = TravelCourseUiModel(),
    val currentAddingDayNumber: Int = 1,
    val draftSchedule: ScheduleItemUiModel? = null,
    val isLoading: Boolean = false
)

sealed interface PlanSharedEvent {
    data class OnCourseNameChanged(val newName: String) : PlanSharedEvent
    data class OnAddScheduleToDay(val targetDay: Int, val newPlace: ScheduleItemUiModel) : PlanSharedEvent
    data class OnDeleteSchedule(val targetDay: Int, val scheduleIdToRemove: String) : PlanSharedEvent
    data class OnReorderSchedules(
        val targetDay: Int,
        val reorderedSchedules: List<ScheduleItemUiModel>
    ) : PlanSharedEvent
    data class OnSetAddingDayNumber(val dayNumber: Int) : PlanSharedEvent
    data class OnSetDraftSchedule(val place: KakaoMapUiModel) : PlanSharedEvent
    data class OnConfirmAndAddSchedule(
        val memoInput: String,
        val accessibilityInfo: AccessibilityInfoUiModel?
    ) : PlanSharedEvent

    data class OnCitySelected(val cityName: String) : PlanSharedEvent
    data class OnDateSelected(val startDate: LocalDate, val endDate: LocalDate) : PlanSharedEvent
    data class OnLoadCourseById(val courseId: String) : PlanSharedEvent

    object OnClearDraftSchedule : PlanSharedEvent
}