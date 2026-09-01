package com.tourdataproject.presentation.viewmodel.plan

import androidx.lifecycle.ViewModel
import com.tourdataproject.domain.usecase.plan.GetRegionPositionUseCase
import com.tourdataproject.presentation.model.RegionUiModel
import com.tourdataproject.presentation.model.course.DayPlanUiModel
import com.tourdataproject.presentation.model.course.TravelCourseUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlanSharedViewModel @Inject constructor(
    private val getRegionPositionUseCase: GetRegionPositionUseCase
) : ViewModel() {

    private val _courseState = MutableStateFlow(
        TravelCourseUiModel()
    )
    val courseState = _courseState.asStateFlow()

    fun setEvent(event: PlanSharedEvent){
        when (event) {
            is PlanSharedEvent.OnCitySelected -> {
                updateRegion(event.cityName)
            }
        }
    }

    fun updateRegion(regionName: String) {
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
        //TODO: 기간 & 몇박며칠 계산
        _courseState.update { currentState ->
            currentState.copy(
                rawStartDate = startDate,
                rawEndDate = endDate,
                )
        }
    }

    fun updateCourseName(newName: String) {
        _courseState.update { currentState ->
            currentState.copy(courseName = newName)
        }
    }

    fun updateRegionPosition(longitude: Double, latitude: Double){
        _courseState.update {
            it.copy(destinationLatitude = latitude, destinationLongitude = longitude)
        }
    }
}

sealed class PlanSharedEvent{
    data class OnCitySelected(val cityName: String): PlanSharedEvent()
}