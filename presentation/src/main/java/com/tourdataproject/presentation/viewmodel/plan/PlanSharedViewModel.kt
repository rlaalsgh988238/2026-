package com.tourdataproject.presentation.viewmodel.plan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.awaitOrThrow
import com.braveberry.data_resource.collectDataResource
import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.domain.model.course.AccessibilityInfo
import com.tourdataproject.presentation.model.KakaoMapUiModel
import com.tourdataproject.presentation.model.RegionUiModel
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.DayPlanUiModel
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

    private val _courseState = MutableStateFlow(
        TravelCourseUiModel()
    )
    val courseState = _courseState.asStateFlow()

    fun setEvent(event: PlanSharedEvent){
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
                Log.d(TAG, "Event: OnDateSelected - startDate: ${event.startDate}, endDate: ${event.endDate}")

                // LocalDate를 Long(Epoch Milliseconds)으로 변환하여 업데이트
                val startMillis = event.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endMillis = event.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                updateDates(startMillis, endMillis)
            }
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

    //TODO: 다음 화면으로 넘어갈 때, stateDate, EndDate만 넘겨주십셔
    fun updateDates(
        startDate: Long,
        endDate: Long
    ) {
        Log.d(TAG, "updateDates: startDate=$startDate, endDate=$endDate")

        //TODO: 기간 & 몇박며칠 계산
        _courseState.update { currentState ->
            currentState.copy(
                rawStartDate = startDate,
                rawEndDate = endDate,
            )
        }
    }

    //TODO :코스 이름 수정 화면 만들시
    fun updateCourseName(newName: String) {
        Log.d(TAG, "updateCourseName: $newName")
        _courseState.update { currentState ->
            currentState.copy(courseName = newName)
        }
    }

    fun updateRegionPosition(longitude: Double, latitude: Double){
        Log.d(TAG, "updateRegionPosition: lat=$latitude, lng=$longitude")
        _courseState.update {
            it.copy(destinationLatitude = latitude, destinationLongitude = longitude)
        }
    }

    //화면 저장 & 삭제 & 재배치 등을 SharedViewModel에서 들고있다가, 마지막에 "저장"버튼을 눌렀을 때, DB로 가게 하는거니까 여기가 맞는듯?
    fun addScheduleToDay(targetDay: Int, newPlace: ScheduleItemUiModel) {
        _courseState.update { currentState ->
            val updatedDayPlans = currentState.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = dayPlan.schedules + newPlace)
                } else dayPlan
            }
            currentState.copy(dayPlans = updatedDayPlans)
        }
    }

    fun deleteSchedule(targetDay: Int, scheduleIdToRemove: String) {
        _courseState.update { currentState ->
            val updatedDayPlans = currentState.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = dayPlan.schedules.filterNot { it.scheduleId == scheduleIdToRemove })
                } else dayPlan
            }
            currentState.copy(dayPlans = updatedDayPlans)
        }
    }

    fun reorderSchedules(targetDay: Int, reorderedSchedules: List<ScheduleItemUiModel>) {
        _courseState.update { currentState ->
            val updatedDayPlans = currentState.dayPlans.map { dayPlan ->
                if (dayPlan.rawDayNumber == targetDay) {
                    dayPlan.copy(schedules = reorderedSchedules)
                } else dayPlan
            }
            currentState.copy(dayPlans = updatedDayPlans)
        }
    }


    val currentAddingDayNumber = MutableStateFlow(1)
    private val _draftSchedule = MutableStateFlow<ScheduleItemUiModel?>(null)
    val draftSchedule = _draftSchedule.asStateFlow()

    //임시 저장
    fun setDraftSchedule(place: KakaoMapUiModel) {
        _draftSchedule.value = ScheduleItemUiModel(
            scheduleId = UUID.randomUUID().toString(), // 고유 ID 부여
            scheduleName = place.placeName,
            latitude = place.y,
            longitude = place.x,
            placeId = place.id,
            address = place.address,
            category = place.category,
            memo = "" // 메모는 다음 화면에서 채울 예정
        )
    }

    // [일정 정보 추가 화면] 저장
    fun confirmAndAddSchedule(
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

    fun clearDraftSchedule() {
        _draftSchedule.value = null
    }

}


sealed class PlanSharedEvent{
    data class OnCitySelected(val cityName: String): PlanSharedEvent()
    data class OnDateSelected(val startDate: LocalDate, val endDate: LocalDate): PlanSharedEvent()
}
