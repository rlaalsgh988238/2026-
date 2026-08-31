package com.tourdataproject.presentation.viewmodel.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.domain.usecase.course.SaveCourseUseCase
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
import javax.inject.Inject
@HiltViewModel
class MakeCourseViewModel @Inject constructor(
    private val saveCourseUseCase: SaveCourseUseCase // SavedStateHandle 삭제!
) : ViewModel() {

    private val _state = MutableStateFlow(CourseState(isLoading = true))
    val state: StateFlow<CourseState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CourseEffect>()
    val effect: SharedFlow<CourseEffect> = _effect.asSharedFlow()

    fun setInitialCourse(course: TravelCourseUiModel) { // 파라미터 타입 맞춰서!
        try {
            android.util.Log.d("CrashCatch", "4. 뷰모델 setInitialCourse 진입함!")
            _state.update {
                it.copy(
                    isLoading = false,
                    course = course
                )
            }
            android.util.Log.d("CrashCatch", "5. 뷰모델 상태 업데이트 성공!")
        } catch (e: Exception) {
            android.util.Log.e("CrashCatch", "🚨 뷰모델 업데이트 중 크래시: ${e.message}", e)
        }
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
                }
            }
            is CourseEvent.OnScheduleItemClicked -> {
                emitEffect(CourseEffect.ShowToast("장소 클릭됨: ${event.scheduleId}"))
            }
            is CourseEvent.OnMapButtonClicked -> emitEffect(CourseEffect.NavigateToMapScreen)
            is CourseEvent.OnSaveButtonClicked -> saveFinalCourseToDB(event.finalCourse)
            is CourseEvent.OnNextButtonClicked -> emitEffect(CourseEffect.NavigateToHomeScreen)

        }
    }


    fun saveFinalCourseToDB(finalCourse: TravelCourseUiModel) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                saveCourseUseCase(finalCourse.toDomain())
                _state.update { it.copy(isSaving = false) }
                emitEffect(CourseEffect.NavigateToHomeScreen)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                emitEffect(CourseEffect.ShowToast("코스 저장에 실패했습니다."))
            }
        }
    }
    private fun emitEffect(effect: CourseEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}