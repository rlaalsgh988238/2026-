package com.tourdataproject.presentation.viewmodel.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.viewmodel.course.makeCourse.uiState.CourseEffect
import com.tourdataproject.presentation.viewmodel.course.makeCourse.uiState.CourseIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MakeCourseViewModel @Inject constructor() : ViewModel() {

    // 🌟 상태(State)와 저장 로직이 완전히 PlanSharedViewModel로 이관되어 제거됨
    private val _effect = MutableSharedFlow<CourseEffect>()
    val effect: SharedFlow<CourseEffect> = _effect.asSharedFlow()

    fun onIntent(intent: CourseIntent) {
        when (intent) {
            is CourseIntent.OnBackButtonClicked -> emitEffect(CourseEffect.NavigateBack)
            is CourseIntent.OnInfoButtonClicked -> emitEffect(CourseEffect.NavigateToCourseInfo)
            is CourseIntent.OnShareButtonClicked -> emitEffect(CourseEffect.ShareCourse)
            is CourseIntent.OnAddScheduleClicked -> emitEffect(CourseEffect.NavigateToAddSchedule(intent.dayNumber))
            is CourseIntent.OnScheduleItemClicked -> emitEffect(CourseEffect.ShowToast("장소 클릭됨: ${intent.scheduleId}"))
            is CourseIntent.OnMapButtonClicked -> emitEffect(CourseEffect.NavigateToMapScreen)
            is CourseIntent.OnEditScheduleButtonClicked -> emitEffect(CourseEffect.NavigateToEditSchedule(intent.dayNumber))
        }
    }

    private fun emitEffect(effect: CourseEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}