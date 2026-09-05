package com.tourdataproject.presentation.viewmodel.course.addSchedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tourdataproject.domain.usecase.calculateAceesibility.CalculateAccessibilityUseCase
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.AccessibilityStatusUiModel
import com.tourdataproject.presentation.utility.Log
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
        Log.d("ToiletDebug", "1. 장소 초기화 됨: name=$name, lat=$lat, lng=$lng")

        _state.update {
            it.copy(placeName = name, address = address, latitude = lat, longitude = lng)
        }

        viewModelScope.launch {
            try {
                Log.d("ToiletDebug", "2. 화장실 UseCase 호출 시작!")
                val domainResult = calculateAccessibilityUseCase(lat, lng)

                // 🌟 핵심 로그: UseCase가 도대체 무슨 값을 뱉어내는지 확인
                Log.d("ToiletDebug", "3. UseCase 계산 완료 -> status: ${domainResult.status}, score: ${domainResult.safetyScore}")

                val uiAccessibility = AccessibilityInfoUiModel(
                    status = AccessibilityStatusUiModel.valueOf(domainResult.status.name),
                    safetyScore = domainResult.safetyScore,
                    planAToiletId = domainResult.planAToiletId,
                    planBToiletId = domainResult.planBToiletId
                )

                _state.update { it.copy(accessibilityInfo = uiAccessibility) }
                Log.d("ToiletDebug", "4. 뷰모델 State 업데이트 완료: $uiAccessibility")

            } catch (e: Exception) {
                val errorMsg = e.message ?: "화장실 정보 계산 실패"
                android.util.Log.e("ToiletError", errorMsg, e)
                Log.e("ToiletDebug", "🚨 화장실 정보 계산 실패", e)
            }
        }
    }

    fun onMemoChanged(memo: String) {
        _state.update { it.copy(memo = memo) }
    }

    fun onSaveClicked() {
        val currentState = _state.value

        // 🌟 저장 직전 로그: SharedViewModel로 넘기기 직전의 상태 확인
        Log.d("ToiletDebug", "5. 저장 버튼 눌림! 넘기는 화장실 데이터: ${currentState.accessibilityInfo}")

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

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