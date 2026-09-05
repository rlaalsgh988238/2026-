package com.tourdataproject.presentation.viewmodel.course.courseList


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.collectDataResource
import com.tourdataproject.domain.usecase.course.GetAllCoursesUseCase
import com.tourdataproject.presentation.mapper.toUiModel
import com.tourdataproject.presentation.model.course.TravelCourseUiModel
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListEffect
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListUiState
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.toCourseListState
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
class CourseListViewModel @Inject constructor(
    private val getAllCoursesUseCase: GetAllCoursesUseCase
) : ViewModel() {
    private val TAG = "CourseListViewModel"
    private val _state = MutableStateFlow(CourseListUiState())
    val state: StateFlow<CourseListUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CourseListEffect>()
    val effect: SharedFlow<CourseListEffect> = _effect.asSharedFlow()

    fun loadCourses() {
        viewModelScope.launch {
            getAllCoursesUseCase().collectDataResource(
                onSuccess = { domainCourses ->
                    Log.d(TAG, "DB에서 코스 ${domainCourses.size}개 불러오기 성공!")
                    val sortedCourses = domainCourses.sortedBy { it.startDate }


                    // 정렬된 리스트를 UI 모델로 변환
                    val uiModels = sortedCourses.map { it.toUiModel() }

                    val newState = uiModels.toCourseListState()

                    // 화면에 쏴주기
                    _state.value = newState
                },
                onError = { error ->
                    Log.e(TAG, "코스 불러오기 실패: ${error.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = "코스 목록을 불러오지 못했습니다."
                        )
                    }
                },
                onLoading = {
                    _state.update { it.copy(isLoading = true) }
                }
            )
        }
    }

    fun onCreatePlanClicked() {
        viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToCreatePlan) }
    }

    fun onRestroomGuideClicked() {
        viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToRestroomGuide) }
    }

    fun onCourseClicked(courseId: String) {
        viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToCourseDetail(courseId)) }
    }

}