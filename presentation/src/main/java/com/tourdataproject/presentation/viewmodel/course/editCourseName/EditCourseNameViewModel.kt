package com.tourdataproject.presentation.viewmodel.course.editCourseName

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.domain.usecase.course.UpdateCourseNameUseCase
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.course.editCourseName.uiState.EditCourseNameEffect
import com.tourdataproject.presentation.viewmodel.course.editCourseName.uiState.EditCourseNameIntent
import com.tourdataproject.presentation.viewmodel.course.editCourseName.uiState.EditCourseNameUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCourseNameViewModel @Inject constructor(
    private val updateCourseNameUseCase: UpdateCourseNameUseCase
) : ViewModel() {
    private val TAG = "EditCourseNameViewModel"

    private val _state = MutableStateFlow(EditCourseNameUiState())
    val state: StateFlow<EditCourseNameUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EditCourseNameEffect>()
    val effect: SharedFlow<EditCourseNameEffect> = _effect.asSharedFlow()

    fun onIntent(intent: EditCourseNameIntent) {
        when (intent) {
            is EditCourseNameIntent.OnSaveClicked -> {
                saveCourseName(intent.courseId, intent.newName)
            }
            is EditCourseNameIntent.OnBackClicked -> {
                viewModelScope.launch { _effect.emit(EditCourseNameEffect.NavigateBack) }
            }
        }
    }

    private fun saveCourseName(courseId: String, newName: String) {
        viewModelScope.launch {
            if (newName.trim().isEmpty()) {
                _effect.emit(EditCourseNameEffect.ShowToast("여행 이름을 입력해주세요."))
                return@launch
            }

            _state.update { it.copy(isLoading = true) } // 저장 시작 (로딩 켬)

            try {
                updateCourseNameUseCase(courseId, newName)

                Log.d(TAG, "이름 변경 성공: $newName")
                _effect.emit(EditCourseNameEffect.ShowToast("여행 이름이 변경되었습니다."))
                _effect.emit(EditCourseNameEffect.NavigateBack) // 완료 후 이전 화면으로 이동

            } catch (e: Exception) {
                Log.e(TAG, "코스 이름 변경 실패", e)
                _effect.emit(EditCourseNameEffect.ShowToast("이름 변경에 실패했습니다."))
            } finally {
                _state.update { it.copy(isLoading = false) } // 저장 끝 (로딩 끔)
            }
        }
    }
}