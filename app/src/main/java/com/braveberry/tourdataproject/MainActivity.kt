package com.braveberry.tourdataproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.braveberry.tourdataproject.screen.kakaoMap.KakaoMapSearchScreen
import com.braveberry.tourdataproject.ui.theme.TourDataProjectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
