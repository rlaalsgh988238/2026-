package com.braveberry.tourdataproject

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.tourdataproject.map_presentation.KakaoMapViewModel

@Composable
fun KakaoMapScreen(
    modifier: Modifier = Modifier,
    viewModel: KakaoMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var mapInstance by remember { mutableStateOf<KakaoMap?>(null) }

    Box(modifier = modifier.fillMaxSize()) {

        // 1. 카카오맵 뷰
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {
                                Log.d("KakaoMap", "지도 소멸됨")
                            }
                            override fun onMapError(error: Exception?) {
                                Log.e("KakaoMap", "지도 에러: ${error?.message}")
                            }
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(kakaoMap: KakaoMap) {
                                Log.d("KakaoMap", "지도 로딩 성공")
                                mapInstance = kakaoMap
                            }
                        }
                    )
                }
            }
        )

        // 2. 검색창 오버레이
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            placeholder = { Text("장소를 검색하세요 (예: 강남역)") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "검색")
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    viewModel.searchPlace()
                    focusManager.clearFocus()
                }
            )
        )

        // 3. 로딩 인디케이터
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // 4. 에러 메시지
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    // 5. 좌표 변경 및 지도 준비 완료 시 카메라 이동
    LaunchedEffect(uiState.targetCoordinate, mapInstance) {
        val coordinate = uiState.targetCoordinate
        val map = mapInstance

        if (coordinate != null && map != null) {
            val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                LatLng.from(coordinate.first, coordinate.second)
            )
            map.moveCamera(cameraUpdate, CameraAnimation.from(500))
        }
    }
}