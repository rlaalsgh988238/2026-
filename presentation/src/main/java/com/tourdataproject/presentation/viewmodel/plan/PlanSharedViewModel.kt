package com.tourdataproject.presentation.viewmodel.plan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.awaitOrThrow
import com.braveberry.data_resource.collectDataResource
import com.braveberry.data_resource.onError
import com.braveberry.data_resource.onSuccess
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.presentation.model.RegionUiModel
import com.tourdataproject.presentation.model.course.DayPlanUiModel
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
}

sealed class PlanSharedEvent{
    data class OnCitySelected(val cityName: String): PlanSharedEvent()
    data class OnDateSelected(val startDate: LocalDate, val endDate: LocalDate): PlanSharedEvent()
}
