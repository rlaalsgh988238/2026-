package com.tourdataproject.presentation.viewmodel.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.domain.usecase.course.SaveCourseUseCase
import com.tourdataproject.presentation.model.course.DayPlanUiModel
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.model.course.TravelCourseUiModel
import com.tourdataproject.presentation.viewmodel.course.uiState.CourseEffect
import com.tourdataproject.presentation.viewmodel.course.uiState.CourseEvent
import com.tourdataproject.presentation.viewmodel.course.uiState.CourseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle, // 🌟 네비게이션 인자(Argument) 수신용
    private val saveCourseUseCase: SaveCourseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CourseState(isLoading = true)) // 처음엔 로딩 상태로 시작
    val state: StateFlow<CourseState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CourseEffect>()
    val effect: SharedFlow<CourseEffect> = _effect.asSharedFlow()

    init {
        val regionName = savedStateHandle.get<String>("regionName") ?: "알 수 없는 지역" //거제 제주 등등
        val startDate = savedStateHandle.get<Long>("startDate") ?: 0L //캘린더에서 받아오기
        val endDate = savedStateHandle.get<Long>("endDate") ?: 0L //캘린더

        initNewCourse(regionName, startDate, endDate)
    }

    private fun initNewCourse(regionName: String, startDate: Long, endDate: Long) {

        val periodFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        val periodString = "${periodFormat.format(Date(startDate))} ~ ${periodFormat.format(Date(endDate))}"
        val tabLabelFormat = SimpleDateFormat("M/d", Locale.KOREA)

        val diffMilliseconds = endDate - startDate
        val totalDays = (diffMilliseconds / (1000 * 60 * 60 * 24)).toInt() + 1

        val initialDayPlans = (1..totalDays).map { dayNumber ->
            val currentDayMillis = startDate + ((dayNumber - 1) * (1000 * 60 * 60 * 24).toLong())

            DayPlanUiModel(
                dayLabel = "${dayNumber}일차",
                dateLabel = tabLabelFormat.format(Date(currentDayMillis)),
                rawDayNumber = dayNumber,
                rawDate = currentDayMillis,
                schedules = emptyList()
            )
        }

        // 3. 만들어진 initialDayPlans를 코스에 주입
        val emptyCourse = TravelCourseUiModel(
            courseId = UUID.randomUUID().toString(),
            destination = regionName, //뭐 거제 등등...
            courseName = "$regionName 여행", //수정 가능 여부?
            datePeriod = periodString,
            rawStartDate = startDate,
            rawEndDate = endDate,
            dayPlans = initialDayPlans
        )

        _state.update { it.copy(isLoading = false, course = emptyCourse) }
    }

    fun onEvent(event: CourseEvent) {
        when (event) {
            is CourseEvent.OnBackButtonClicked -> emitEffect(CourseEffect.NavigateBack)
            is CourseEvent.OnInfoButtonClicked -> emitEffect(CourseEffect.NavigateToCourseInfo)
            is CourseEvent.OnShareButtonClicked -> emitEffect(CourseEffect.ShareCourse)
            is CourseEvent.OnAddScheduleClicked -> {
                val currentCourseId = state.value.course?.courseId
                if (currentCourseId != null) {
                    emitEffect(CourseEffect.NavigateToAddSchedule(currentCourseId, event.dayNumber))
                } else {
                    emitEffect(CourseEffect.ShowToast("코스 정보를 찾을 수 없습니다."))
                }
            }

            is CourseEvent.OnScheduleItemClicked -> {
                emitEffect(CourseEffect.ShowToast("장소 클릭됨: ${event.scheduleId}"))
            }

            is CourseEvent.OnMapButtonClicked -> emitEffect(CourseEffect.NavigateToMapScreen)

            is CourseEvent.OnNextButtonClicked -> saveAndNavigateToNext()
        }
    }

    //일정 추가
    fun addSchedule(targetDay: Int, newPlace: ScheduleItemUiModel) {
        //TODO: 에러처리 고려
        val currentCourse = state.value.course ?: return

        val updatedDayPlans = currentCourse.dayPlans.map { dayPlan ->
            if (dayPlan.rawDayNumber == targetDay) {
                dayPlan.copy(schedules = dayPlan.schedules + newPlace)
            } else {
                dayPlan
            }
        }

        _state.update { it.copy(course = currentCourse.copy(dayPlans = updatedDayPlans)) }
    }

    private fun saveAndNavigateToNext() {
        val currentUiCourse = state.value.course ?: return

        //TODO: 로딩 추가
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                val domainCourse = currentUiCourse.toDomain()
                saveCourseUseCase(domainCourse)
                _state.update { it.copy(isSaving = false) }
                emitEffect(CourseEffect.NavigateToNextScreen)

            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                emitEffect(CourseEffect.ShowToast("코스 저장에 실패했습니다: ${e.localizedMessage}"))
            }
        }
    }

    private fun emitEffect(effect: CourseEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}