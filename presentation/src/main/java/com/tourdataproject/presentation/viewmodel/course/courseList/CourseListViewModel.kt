package com.tourdataproject.presentation.viewmodel.course.courseList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.collectDataResource
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
    private val clearPlanStateBackupUseCase: ClearPlanStateBackupUseCase
) : ViewModel() {
    private val TAG = "CourseListViewModel"

    private val _state = MutableStateFlow(CourseListUiState())
    val state: StateFlow<CourseListUiState> = _state.asStateFlow()

    // 현재 선택된 필터 상태 관리
    private val _selectedFilter = MutableStateFlow(TravelFilter.ALL)
    val selectedFilter: StateFlow<TravelFilter> = _selectedFilter.asStateFlow()

    // 필터링 전 전체 리스트 보관용
    private var allCourseItems: List<CourseListItemState> = emptyList()

    private val _effect = MutableSharedFlow<CourseListEffect>()
    val effect: SharedFlow<CourseListEffect> = _effect.asSharedFlow()

    fun loadCourses() {
        viewModelScope.launch {
            clearPlanStateBackupUseCase().collectDataResource(
                onSuccess = { Log.d(TAG, "백업데이터 삭제 성공")},
                onError = { Log.d(TAG, "백업데이터 삭제 실패")}
            )
            getAllCoursesUseCase().collectDataResource(
                onSuccess = { domainCourses ->
                    val sortedCourses = domainCourses.sortedBy { it.startDate }
                    val uiModels = sortedCourses.map { it.toUiModel() }

                    // 매퍼를 통해 전체 아이템 리스트 생성 및 보관
                    val fullState = uiModels.toCourseListState()
                    allCourseItems = fullState.courses

                    // 현재 선택된 필터 적용하여 상태 업데이트
                    applyFilter(_selectedFilter.value)
                },
                onError = { error ->
                    _state.update { it.copy(isLoading = false, isError = true) }
                },
                onLoading = { _state.update { it.copy(isLoading = true) } }
            )
        }
    }

    // 필터 변경 시 호출되는 함수
    fun onFilterChanged(filter: TravelFilter) {
        _selectedFilter.value = filter
        applyFilter(filter)
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

    fun onCreatePlanClicked() { viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToCreatePlan) } }
    fun onRestroomGuideClicked() { viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToRestroomGuide) } }
    fun onCourseClicked(courseId: String) { viewModelScope.launch { _effect.emit(CourseListEffect.NavigateToCourseDetail(courseId)) } }
}
