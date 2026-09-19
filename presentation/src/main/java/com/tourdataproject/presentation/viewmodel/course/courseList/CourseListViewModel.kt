package com.tourdataproject.presentation.viewmodel.course.courseList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.collectDataResource
import com.tourdataproject.domain.usecase.course.DeleteCourseUseCase // 추가
import com.tourdataproject.domain.usecase.course.GetAllCoursesUseCase
import com.tourdataproject.domain.usecase.plan.backUp.ClearPlanStateBackupUseCase
import com.tourdataproject.presentation.mapper.toUiModel
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val getAllCoursesUseCase: GetAllCoursesUseCase,
    private val clearPlanStateBackupUseCase: ClearPlanStateBackupUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase
) : ViewModel() {
    private val TAG = "CourseListViewModel"

    private val _state = MutableStateFlow(CourseListUiState())
    val state: StateFlow<CourseListUiState> = _state.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TravelFilter.ALL)
    val selectedFilter: StateFlow<TravelFilter> = _selectedFilter.asStateFlow()

    private var allCourseItems: List<CourseListItemState> = emptyList()

    private val _effect = MutableSharedFlow<CourseListEffect>()
    val effect: SharedFlow<CourseListEffect> = _effect.asSharedFlow()

    fun onIntent(intent: CourseListIntent) {
        when (intent) {
            is CourseListIntent.OnLoadCourses -> loadCourses()
            is CourseListIntent.OnFilterChanged -> {
                _selectedFilter.value = intent.filter
                applyFilter(intent.filter)
            }
            is CourseListIntent.OnCreatePlanClicked ->
                viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToCreatePlan) }
            is CourseListIntent.OnRestroomGuideClicked ->
                viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToRestroomGuide) }
            is CourseListIntent.OnCourseClicked ->
                viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToCourseDetail(intent.courseId)) }
            is CourseListIntent.OnDeleteCourseClicked ->
                deleteCourse(intent.courseId)
        }
    }

    private fun loadCourses() {
        viewModelScope.launch {
            clearPlanStateBackupUseCase().collectDataResource(
                onSuccess = { Log.d(TAG, "백업데이터 삭제 성공") },
                onError = { Log.d(TAG, "백업데이터 삭제 실패") }
            )
            getAllCoursesUseCase().collectDataResource(
                onSuccess = { domainCourses ->
                    val sortedCourses = domainCourses.sortedBy { it.startDate }
                    val uiModels = sortedCourses.map { it.toUiModel() }
                    val fullState = uiModels.toCourseListState()
                    allCourseItems = fullState.courses
                    applyFilter(_selectedFilter.value)
                },
                onError = { error ->
                    _state.update { it.copy(isLoading = false, isError = true) }
                },
                onLoading = { _state.update { it.copy(isLoading = true) } }
            )
        }
    }

    private fun applyFilter(filter: TravelFilter) {
        val filteredList = when (filter) {
            TravelFilter.ALL -> allCourseItems
            TravelFilter.UPCOMING -> allCourseItems.filter {
                it.dDayText.startsWith("D-") || it.dDayText == "D-Day"
            }
            TravelFilter.COMPLETED -> allCourseItems.filter {
                it.dDayText.startsWith("D+")
            }
        }
        _state.update { it.copy(courses = filteredList, isLoading = false) }
    }

    private fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            try {
                deleteCourseUseCase(courseId)
                loadCourses() // 삭제 후 목록 다시 불러오기
                _effect.emit(CourseListEffect.ShowToast("코스가 삭제되었습니다."))
            } catch (e: Exception) {
                Log.e(TAG, "코스 삭제 실패", e)
                _effect.emit(CourseListEffect.ShowToast("삭제에 실패했습니다."))
            }
        }
    }
}