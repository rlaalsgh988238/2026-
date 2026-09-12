package com.tourdataproject.presentation.viewmodel.toilet

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braveberry.data_resource.collectDataResource
import com.tourdataproject.domain.usecase.user_data.TrackUserLocationUseCase
import com.tourdataproject.domain.usecase.getToIlet.GetToiletsByDistanceUseCase
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.toilet.uiState.NearbyToiletEffect
import com.tourdataproject.presentation.viewmodel.toilet.uiState.NearbyToiletIntent
import com.tourdataproject.presentation.viewmodel.toilet.uiState.NearbyToiletState
import com.tourdataproject.presentation.viewmodel.toilet.uiState.ToiletItemPresentationModel
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
class NearbyToiletViewModel @Inject constructor(
    private val trackUserLocationUseCase: TrackUserLocationUseCase,
    private val getToiletsByDistanceUseCase: GetToiletsByDistanceUseCase
) : ViewModel() {
    private val TAG = "NearbyToiletViewModel"

    private val _state = MutableStateFlow(NearbyToiletState())
    val state: StateFlow<NearbyToiletState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<NearbyToiletEffect>()
    val effect: SharedFlow<NearbyToiletEffect> = _effect.asSharedFlow()

    init {
        Log.d(TAG, "NearbyToiletViewModel 초기화 완료. 주변 화장실 로드 시작")
        onIntent(NearbyToiletIntent.LoadToilets)
    }

    fun onIntent(intent: NearbyToiletIntent) {
        when (intent) {
            is NearbyToiletIntent.LoadToilets -> {
                Log.d(TAG, "Intent 수신: LoadToilets")
                fetchLocationAndToilets()
            }

            is NearbyToiletIntent.OnToiletGuideClicked -> {
                Log.d(TAG, "Intent 수신: OnToiletGuideClicked (목적지: ${intent.targetToilet.name})")
                val currentLocation = _state.value.currentLocation
                if (currentLocation != null) {
                    viewModelScope.launch {
                        _effect.emit(
                            NearbyToiletEffect.NavigateToExternalMap(
                                startLat = currentLocation.first,
                                startLng = currentLocation.second,
                                destLat = intent.targetToilet.lat,
                                destLng = intent.targetToilet.lng
                            )
                        )
                    }
                } else {
                    Log.e(TAG, "길찾기 실패: 현재 위치 정보가 비어있음")
                    viewModelScope.launch {
                        _effect.emit(NearbyToiletEffect.ShowToast("현재 위치를 찾을 수 없습니다."))
                    }
                }
            }

            is NearbyToiletIntent.OnBackClicked -> {
                Log.d(TAG, "Intent 수신: OnBackClicked")
                viewModelScope.launch { _effect.emit(NearbyToiletEffect.NavigateBack) }
            }
        }
    }

    private fun fetchLocationAndToilets() {
        viewModelScope.launch {
            Log.d(TAG, "유저 위치 추적 시작...")
            trackUserLocationUseCase().collectDataResource(
                onLoading = {
                    Log.d(TAG, "유저 위치 로딩 중...")
                    _state.update { it.copy(isLoading = true) }
                },
                onSuccess = { location ->
                    Log.d(TAG, "유저 위치 획득 성공: lat=${location.latitude}, lng=${location.longitude}")
                    _state.update {
                        it.copy(currentLocation = Pair(location.latitude, location.longitude))
                    }

                    // 위치 기반으로 화장실 리스트 조회 실행
                    loadToiletsFromLocation(location.latitude, location.longitude)
                },
                onError = { error ->
                    Log.e(TAG, "유저 위치 획득 실패: ${error.message}")
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "위치를 가져오지 못했습니다.")
                    }
                    viewModelScope.launch { _effect.emit(NearbyToiletEffect.ShowToast("위치를 가져오지 못했습니다.")) }
                }
            )
        }
    }

    private fun loadToiletsFromLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            val searchRadius = 1000f // 탐색 반경 1km
            Log.d(TAG, "화장실 정보 요청 시작 (반경: ${searchRadius}m, lat=$lat, lng=$lng)")

            getToiletsByDistanceUseCase(searchRadius, lat, lng).collectDataResource(
                onLoading = {
                    Log.d(TAG, "화장실 정보 로딩 중...")
                },
                onSuccess = { domainToilets ->
                    Log.d(TAG, "화장실 정보 로드 성공: 총 ${domainToilets.size}개 발견")

                    // 1. 유저 위치와 화장실 위치 간의 거리를 계산하여 (화장실, 거리) 묶음으로 생성
                    val toiletsWithDistance = domainToilets.map { toilet ->
                        val results = FloatArray(1)
                        // 안드로이드 기본 Location 클래스를 사용하여 두 좌표 간의 실제 거리(미터) 계산
                        Location.distanceBetween(
                            lat, lng,
                            toilet.latitude, toilet.longitude,
                            results
                        )
                        toilet to results[0] // Pair(화장실 객체, 계산된 거리) 반환
                    }

                    // 2. 계산된 거리를 기준으로 오름차순 정렬 (가장 가까운 화장실이 위로 오도록)
                    val sortedToilets = toiletsWithDistance.sortedBy { it.second }

                    // 3. UI 모델로 변환
                    val uiModels = sortedToilets.map { (toilet, distance) ->
                        ToiletItemPresentationModel(
                            name = toilet.toiletName, // name -> toiletName 수정
                            // 도로명 주소를 우선 사용하고, 없으면 지번 주소, 둘 다 없으면 "주소 미상" 처리
                            address = toilet.roadAddress ?: toilet.lotAddress ?: "주소 미상",
                            distance = "${distance.toInt()}m", // 소수점 버리고 정수(예: 120m)로 표기
                            lat = toilet.latitude,
                            lng = toilet.longitude
                        )
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            toilets = uiModels,
                            errorMessage = null
                        )
                    }
                    Log.d(TAG, "UI State 업데이트 완료 (화장실 리스트 갱신됨)")
                },
                onError = { error ->
                    Log.e(TAG, "화장실 정보 로드 실패: ${error.message}", error)
                    _state.update {
                        it.copy(isLoading = false, errorMessage = "화장실 정보를 불러오지 못했습니다.")
                    }
                }
            )
        }
    }
    }