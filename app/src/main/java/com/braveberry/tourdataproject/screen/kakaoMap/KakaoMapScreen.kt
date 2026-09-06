package com.braveberry.tourdataproject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.tourdataproject.presentation.utility.Log

@Composable
fun KakaoMapScreen(
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit,
    targetCoordinate: Pair<Double, Double>? = null
) {
    var mapInstance by remember { mutableStateOf<KakaoMap?>(null) }

    Box(modifier = modifier.fillMaxSize()) {

        //카카오맵 뷰
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() { Log.d("KakaoMap", "지도 소멸됨") }
                            override fun onMapError(error: Exception?) { Log.e("KakaoMap", "지도 에러: ${error?.message}") }
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

        // 2. 가짜 검색창 (클릭 시 onNavigateToSearch 실행)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
                //검색화면 이동
                .clickable { onNavigateToSearch() },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "검색")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "장소를 검색하세요 (예: 스타벅스)", color = Color.Gray)
            }
        }
    }

    // 3. targetCoordinate가 변경되면 해당 좌표로 카메라 이동
    LaunchedEffect(targetCoordinate, mapInstance) {
        val map = mapInstance
        if (targetCoordinate != null && map != null) {
            val cameraUpdate = CameraUpdateFactory.newCenterPosition(
                LatLng.from(targetCoordinate.first, targetCoordinate.second)
            )
            map.moveCamera(cameraUpdate, CameraAnimation.from(500))
        }
    }
}