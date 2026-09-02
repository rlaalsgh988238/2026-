package com.tourdataproject.presentation.viewmodel.plan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.collectDataResource
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.presentation.model.KakaoMapUiModel
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.model.course.TravelCourseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlanSharedViewModel @Inject constructor(
    private val getRegionPositionUseCase: GetRegionPositionUseCase
) : ViewModel() {

    private val TAG = "PlanSharedViewModel"

    private val _courseState = MutableStateFlow(TravelCourseUiModel())
    val courseState = _courseState.asStateFlow()

    // ================= 이벤트 진입점 =================

    fun setEvent(event: PlanSharedEvent) {
        when (event) {
            is PlanSharedEvent.OnCitySelected -> handleCitySelected(event.cityName)
            is PlanSharedEvent.OnDateSelected -> handleDateSelected(event.startDate, event.endDate)
        }
    }

    // 지역명 저장 + 좌표 조회를 한 번에 처리
    private fun handleCitySelected(cityName: String) {
        Log.d(TAG, "OnCitySelected: $cityName")
        updateRegion(cityName)

        viewModelScope.launch {
            getRegionPositionUseCase(cityName).collectDataResource(
                onSuccess = { location ->
                    Log.d(TAG, "position success: $location")
                    updateRegionPosition(location.longitude, location.latitude)
                },
                onError = { error ->
                    Log.e(TAG, "position error: ${error.message}")
                },
                onLoading = {
                    Log.d(TAG, "position loading...")
                }
            )
        }
    }

    private fun handleDateSelected(startDate: LocalDate, endDate: LocalDate) {
        Log.d(TAG, "OnDateSelected: $startDate ~ $endDate")
        val zone = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = endDate.atStartOfDay(zone).toInstant().toEpochMilli()
        updateDates(startMillis, endMillis)
    }

    // ================= 상태 갱신 (내부 전용) =================

    private fun updateRegion(regionName: String) {
        _courseState.update { current ->
            current.copy(
                destination = regionName,
                courseName = "${regionName} 여행"
            )
        }
    }

    private fun updateRegionPosition(longitude: Double, latitude: Double) {
        _courseState.update {
            it.copy(destinationLatitude = latitude, destinationLongitude = longitude)
        }
    }

    private fun updateDates(startDate: Long, endDate: Long) {
        // TODO: n박 n일 datePeriod 계산 추가 예정
        _courseState.update { current ->
            current.copy(
                rawStartDate = startDate,
                rawEndDate = endDate
            )
        }
    }

    // ================= 코스 이름 수정 =================

    fun updateCourseName(newName: String) {
        _courseState.update { it.copy(courseName = newName) }
    }

    // ================= 일정(스케줄) 관리 =================

    fun addScheduleToDay(targetDay: Int, newPlace: ScheduleItemUiModel) {
        _courseState.update { current ->
            val updated = current.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = dayPlan.schedules + newPlace)
                } else dayPlan
            }
            current.copy(dayPlans = updated)
        }
    }

    fun deleteSchedule(targetDay: Int, scheduleIdToRemove: String) {
        _courseState.update { current ->
            val updated = current.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = dayPlan.schedules.filterNot { it.scheduleId == scheduleIdToRemove })
                } else dayPlan
            }
            current.copy(dayPlans = updated)
        }
    }

    fun reorderSchedules(targetDay: Int, reorderedSchedules: List<ScheduleItemUiModel>) {
        _courseState.update { current ->
            val updated = current.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = reorderedSchedules)
                } else dayPlan
            }
            current.copy(dayPlans = updated)
        }
    }

    // ================= 스케줄 임시 저장(Draft) =================

    val currentAddingDayNumber = MutableStateFlow(1)
    private val _draftSchedule = MutableStateFlow<ScheduleItemUiModel?>(null)
    val draftSchedule = _draftSchedule.asStateFlow()

    fun setDraftSchedule(place: KakaoMapUiModel) {
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

    fun confirmAndAddSchedule(
        memoInput: String,
        accessibilityInfo: AccessibilityInfoUiModel?
    ) {
        val draft = _draftSchedule.value ?: return

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
            } else dayPlan
        }

        _courseState.value = currentCourse.copy(dayPlans = updatedDayPlans)
        _draftSchedule.value = null
    }

    fun clearDraftSchedule() {
        _draftSchedule.value = null
    }
}

sealed class PlanSharedEvent {
    data class OnCitySelected(val cityName: String) : PlanSharedEvent()
    data class OnDateSelected(val startDate: LocalDate, val endDate: LocalDate) : PlanSharedEvent()
}