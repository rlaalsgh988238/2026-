package com.tourdataproject.presentation.viewmodel.plan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.collectDataResource
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.presentation.model.KakaoMapUiModel
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.DayPlanUiModel
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.model.course.TravelCourseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List

@HiltViewModel
class PlanSharedViewModel @Inject constructor(
    private val getRegionPositionUseCase: GetRegionPositionUseCase
) : ViewModel() {

    private val TAG = "PlanSharedViewModel"

    private val _courseState = MutableStateFlow(
        TravelCourseUiModel()
    )
    val courseState = _courseState.asStateFlow()

    fun setEvent(event: PlanSharedEvent) {
        when (event) {
            is PlanSharedEvent.OnCitySelected -> {
                Log.d(TAG, "Event: OnCitySelected - cityName: ${event.cityName}")
                updateRegion(event.cityName)
                viewModelScope.launch {
                    getRegionPositionUseCase(event.cityName).collectDataResource(
                        onSuccess = { location ->
                            Log.d(TAG, "Success: $location")
                            updateRegionPosition(location.longitude, location.latitude)
                        },
                        onError = { error ->
                            Log.e(TAG, "Error: ${error.message}")
                        },
                        onLoading = {
                            Log.d(TAG, "Loading...")
                        }
                    )
                }
            }

            is PlanSharedEvent.OnDateSelected -> {
                Log.d(
                    TAG,
                    "Event: OnDateSelected - startDate: ${event.startDate}, endDate: ${event.endDate}"
                )

                // LocalDate를 Long(Epoch Milliseconds)으로 변환하여 업데이트
                val startMillis =
                    event.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endMillis =
                    event.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                updateDates(startMillis, endMillis)
            }

            is PlanSharedEvent.OnCourseNameChanged -> updateCourseName(event.newName)
            is PlanSharedEvent.OnAddScheduleToDay -> addScheduleToDay(
                event.targetDay,
                event.newPlace
            )

            is PlanSharedEvent.OnDeleteSchedule -> deleteSchedule(
                event.targetDay,
                event.scheduleIdToRemove
            )

            is PlanSharedEvent.OnReorderSchedules -> reorderSchedules(
                event.targetDay,
                event.reorderedSchedules
            )

            is PlanSharedEvent.OnSetAddingDayNumber -> currentAddingDayNumber.value =
                event.dayNumber

            is PlanSharedEvent.OnSetDraftSchedule -> setDraftSchedule(event.place)
            is PlanSharedEvent.OnConfirmAndAddSchedule -> confirmAndAddSchedule(
                event.memoInput,
                event.accessibilityInfo
            )

            PlanSharedEvent.OnClearDraftSchedule -> clearDraftSchedule()
        }
    }

    //TODO:좌표값도 받아오기
    fun updateRegion(regionName: String) {
        Log.d(TAG, "updateRegion: $regionName")
        _courseState.update { currentState ->
            currentState.copy(
                destination = regionName,
                courseName = "${regionName} 여행"
            )
        }
    }


    fun updateDates(
        startDate: Long,
        endDate: Long
    ) {
        Log.d(TAG, "updateDates: startDate=$startDate, endDate=$endDate")

        // 1. Long(밀리초) 타임스탬프를 LocalDate로 변환
        val startLocalDate = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val endLocalDate = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate()

        // 2. datePeriod 포맷팅 ("yyyy.MM.dd ~ yyyy.MM.dd")
        val periodFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val datePeriodString = "${startLocalDate.format(periodFormatter)} ~ ${endLocalDate.format(periodFormatter)}"

        // 3. 총 여행 일수 계산 (예: 시작일과 종료일이 같으면 1일차 하나만, 차이가 2일이면 총 3일)
        val totalDays = ChronoUnit.DAYS.between(startLocalDate, endLocalDate).toInt() + 1

        // 4. dayPlans 리스트 동적 생성
        val dateLabelFormatter = DateTimeFormatter.ofPattern("M/dd") // "9/01" 형태로 출력
        val generatedDayPlans = (0 until totalDays).map { i ->
            val currentDate = startLocalDate.plusDays(i.toLong())
            DayPlanUiModel(
                dayLabel = "${i + 1}일차",
                dateLabel = currentDate.format(dateLabelFormatter),
                rawDayNumber = i + 1,
                schedules = emptyList() // 초기화 시점에는 빈 리스트
            )
        }

        // 5. State 업데이트
        _courseState.update { currentState ->
            currentState.copy(
                rawStartDate = startDate,
                rawEndDate = endDate,
                datePeriod = datePeriodString,
                dayPlans = generatedDayPlans
            )
        }
    }



    fun updateRegionPosition(longitude: Double, latitude: Double) {
        Log.d(TAG, "updateRegionPosition: lat=$latitude, lng=$longitude")
        _courseState.update {
            it.copy(destinationLatitude = latitude, destinationLongitude = longitude)
        }
    }

    val currentAddingDayNumber = MutableStateFlow(1)

    private val _draftSchedule = MutableStateFlow<ScheduleItemUiModel?>(null)
    val draftSchedule = _draftSchedule.asStateFlow()

    private fun updateCourseName(newName: String) {
        Log.d(TAG, "updateCourseName: $newName")
        _courseState.update { it.copy(courseName = newName) }
    }

    private fun addScheduleToDay(targetDay: Int, newPlace: ScheduleItemUiModel) {
        _courseState.update { currentState ->
            val updatedDayPlans = currentState.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = dayPlan.schedules + newPlace)
                } else dayPlan
            }
            currentState.copy(dayPlans = updatedDayPlans)
        }
    }

    private fun deleteSchedule(targetDay: Int, scheduleIdToRemove: String) {
        _courseState.update { currentState ->
            val updatedDayPlans = currentState.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = dayPlan.schedules.filterNot { it.scheduleId == scheduleIdToRemove })
                } else dayPlan
            }
            currentState.copy(dayPlans = updatedDayPlans)
        }
    }

    private fun reorderSchedules(targetDay: Int, reorderedSchedules: List<ScheduleItemUiModel>) {
        _courseState.update { currentState ->
            val updatedDayPlans = currentState.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = reorderedSchedules)
                } else dayPlan
            }
            currentState.copy(dayPlans = updatedDayPlans)
        }
    }

    private fun setDraftSchedule(place: KakaoMapUiModel) {
        _draftSchedule.value = ScheduleItemUiModel(
            scheduleId = UUID.randomUUID().toString(),
            scheduleName = place.placeName,
            latitude = place.y,
            longitude = place.x,
            placeId = place.id,
            address = place.address,
            category = place.category,
            memo = ""
        )
    }

    private fun confirmAndAddSchedule(
        memoInput: String,
        accessibilityInfo: AccessibilityInfoUiModel?
    ) {
        val draft = _draftSchedule.value ?: return

        //깡통 객체 처리
        val finalSchedule = draft.copy(
            memo = memoInput,
            accessibilityInfo = accessibilityInfo ?: AccessibilityInfoUiModel()
        )

        val currentCourse = _courseState.value
        val targetDayNum = currentAddingDayNumber.value

        val updatedDayPlans = currentCourse.dayPlans.map { dayPlan ->
            if (dayPlan.rawDayNumber == targetDayNum) {
                dayPlan.copy(
                    schedules = dayPlan.schedules + finalSchedule.copy(order = dayPlan.schedules.size + 1)
                )
            } else {
                dayPlan
            }
        }

        _courseState.value = currentCourse.copy(dayPlans = updatedDayPlans)
        _draftSchedule.value = null
    }

    private fun clearDraftSchedule() {
        _draftSchedule.value = null
    }
}


data class PlanSharedState(
    val course: TravelCourseUiModel = TravelCourseUiModel(),
    val currentAddingDayNumber: Int = 1,
    val draftSchedule: ScheduleItemUiModel? = null,
    val isLoading: Boolean = false
)

sealed interface PlanSharedEvent {

    data class OnCitySelected(val cityName: String) : PlanSharedEvent
    data class OnDateSelected(val startDate: LocalDate, val endDate: LocalDate) : PlanSharedEvent
    data class OnCourseNameChanged(val newName: String) : PlanSharedEvent
    data class OnAddScheduleToDay(val targetDay: Int, val newPlace: ScheduleItemUiModel) :
        PlanSharedEvent

    data class OnDeleteSchedule(val targetDay: Int, val scheduleIdToRemove: String) :
        PlanSharedEvent

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

    object OnClearDraftSchedule : PlanSharedEvent
}