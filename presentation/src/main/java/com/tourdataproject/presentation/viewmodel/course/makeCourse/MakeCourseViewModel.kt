package com.tourdataproject.presentation.viewmodel.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.presentation.viewmodel.course.makeCourse.uiState.CourseEffect
import com.tourdataproject.presentation.viewmodel.course.makeCourse.uiState.CourseEffect.*
import com.tourdataproject.presentation.viewmodel.course.makeCourse.uiState.CourseIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MakeCourseViewModel @Inject constructor() : ViewModel() {

    private val _effect = MutableSharedFlow<CourseEffect>()
    val effect: SharedFlow<CourseEffect> = _effect.asSharedFlow()

    fun onIntent(intent: CourseIntent) {
        when (intent) {
            is CourseIntent.OnBackButtonClicked -> emitEffect(NavigateBack)
            is CourseIntent.OnInfoButtonClicked -> emitEffect(NavigateToCourseInfo)
            is CourseIntent.OnShareButtonClicked -> emitEffect(ShareCourse)
            is CourseIntent.OnAddScheduleClicked -> emitEffect(NavigateToAddSchedule(intent.dayNumber))
            is CourseIntent.OnScheduleItemClicked -> emitEffect(ShowToast("장소 클릭됨: ${intent.scheduleId}"))
            is CourseIntent.OnMapButtonClicked -> emitEffect(NavigateToMapScreen)
            is CourseIntent.OnEditScheduleButtonClicked -> emitEffect(NavigateToEditSchedule(intent.dayNumber))
            is CourseIntent.OnAddStayButtonClicked -> emitEffect(NavigateToAddStay)
        }
    }

    private fun emitEffect(effect: CourseEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}