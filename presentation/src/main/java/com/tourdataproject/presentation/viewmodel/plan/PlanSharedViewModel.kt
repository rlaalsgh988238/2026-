package com.tourdataproject.presentation.viewmodel.plan

import androidx.lifecycle.ViewModel
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
class PlanSharedViewModel @Inject constructor() : ViewModel() {
//TODO
    /*
    * data class TravelCourseUiModel(
    val courseId: String = UUID.randomUUID().toString(),
    val destination: String = "",
    val courseName: String = "",
    val datePeriod: String = "",
    val rawStartDate: Long = 0L,
    val rawEndDate: Long = 0L,
    val dayPlans: List<DayPlanUiModel> = emptyList()
)
*이렇게 채워놓고 써야하나 근데 이럴거면 음...중복코드 아닌가 모델이랑 뭔가  */
    private val _courseState = MutableStateFlow(
        TravelCourseUiModel(
            courseId = UUID.randomUUID().toString(),
            destination = "",
            courseName = "",
            datePeriod = "", //"2박 3일"
            rawStartDate = 0L,
            rawEndDate = 0L,
            dayPlans = emptyList()
        )
    )
    val courseState = _courseState.asStateFlow()


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


}

