package com.braveberry.tourdataproject

import android.Manifest // 🌟 추가됨
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts // 🌟 추가됨
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.braveberry.tourdataproject.screen.kakaoMap.KakaoMapSearchScreen
// import com.braveberry.tourdataproject.screen.kakaoMap.KakaoMapScreen // (경로에 맞게 임포트 확인)
import com.braveberry.tourdataproject.ui.theme.TourDataProjectTheme
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 🌟 1. 권한 요청 결과를 처리하는 런처 등록 (클래스 최상단에 위치해야 합니다)
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // 정확한 위치 권한 허용됨 (이제 DataSource가 정상적으로 ByteArray를 가져옵니다)
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // 대략적인 위치 권한 허용됨
            }
            else -> {
                // 권한 거부됨 (우리가 짠 로직대로 Repository가 알아서 null로 처리하고 전국구 검색을 실행합니다)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🌟 2. 앱이 켜지자마자 권한 요청 팝업 띄우기 실행
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        enableEdgeToEdge()
        setContent {
            TourDataProjectTheme {
                var isSearching by remember { mutableStateOf(false) }
                var selectedCoordinate by remember { mutableStateOf<Pair<Double, Double>?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (isSearching) {
                        // 1. 검색 화면
                        KakaoMapSearchScreen(
                            modifier = Modifier.padding(innerPadding),
                            onBackClick = {
                                isSearching = false
                            },
                            onPlaceSelected = { x, y ->
                                selectedCoordinate = Pair(y, x) // 카카오맵 API는 보통 (경도:x, 위도:y) 이므로 필요시 순서 조정
                                isSearching = false
                            }
                        )
                    } else {
                        // 메인 지도 화면
                        KakaoMapScreen(
                            modifier = Modifier.padding(innerPadding),
                            onNavigateToSearch = {
                                isSearching = true
                            },
                            targetCoordinate = selectedCoordinate
                        )
                    }
                }
            }
        }
    }
}

