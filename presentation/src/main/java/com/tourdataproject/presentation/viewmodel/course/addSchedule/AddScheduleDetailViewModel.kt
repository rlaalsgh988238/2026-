package com.tourdataproject.presentation.viewmodel.course.addSchedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.domain.usecase.calculateAceesibility.CalculateAccessibilityUseCase
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.AccessibilityStatusUiModel
import com.tourdataproject.presentation.viewmodel.course.addSchedule.uiState.AddScheduleDetailEffect
import com.tourdataproject.presentation.viewmodel.course.addSchedule.uiState.AddScheduleDetailUiState
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
class AddScheduleDetailViewModel @Inject constructor(
    private val calculateAccessibilityUseCase: CalculateAccessibilityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddScheduleDetailUiState())
    val state: StateFlow<AddScheduleDetailUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddScheduleDetailEffect>()
    val effect: SharedFlow<AddScheduleDetailEffect> = _effect.asSharedFlow()


    fun setInitialPlace(name: String, address: String, lat: Double, lng: Double) {
        _state.update {
            it.copy(placeName = name, address = address, latitude = lat, longitude = lng)
        }

        viewModelScope.launch {
            try {
                //화장실 위험도 계산
                val domainResult = calculateAccessibilityUseCase(lat, lng)
                val uiAccessibility = AccessibilityInfoUiModel(
                    status = AccessibilityStatusUiModel.valueOf(domainResult.status.name),
                    safetyScore = domainResult.safetyScore,
                    planAToiletId = domainResult.planAToiletId,
                    planBToiletId = domainResult.planBToiletId
                )


                _state.update { it.copy(accessibilityInfo = uiAccessibility) }
            } catch (e: Exception) {
                android.util.Log.e("CalculateError", "화장실 정보 계산 실패", e)
            }
        }
    }

    fun onMemoChanged(memo: String) {
        _state.update { it.copy(memo = memo) }
    }

    // 3. '저장' 버튼 클릭 시
    fun onSaveClicked() {
        val currentState = _state.value

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            // 현재 State에 모인 모든 데이터(유저 입력 정보 + 미리 계산해둔 화장실 정보)를 묶어서 Effect로 방출
            _effect.emit(
                AddScheduleDetailEffect.SubmitSchedule(
                    placeName = currentState.placeName,
                    address = currentState.address,
                    latitude = currentState.latitude,
                    longitude = currentState.longitude,
                    memo = currentState.memo,
                    accessibilityInfo = currentState.accessibilityInfo
                )
            )
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch { _effect.emit(AddScheduleDetailEffect.NavigateBack) }
    }
}