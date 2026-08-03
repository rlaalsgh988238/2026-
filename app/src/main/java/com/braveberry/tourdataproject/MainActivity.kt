package com.braveberry.tourdataproject

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.braveberry.tourdataproject.ui.theme.TourDataProjectTheme
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            TourDataProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KakaoMapScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun KakaoMapScreen(modifier: Modifier = Modifier) {
    // Compose 안에서 기존 Android View를 렌더링하게 해주는 AndroidView
    AndroidView(
        modifier = modifier.fillMaxSize(), // 화면을 꽉 채우도록 설정
        factory = { context ->
            MapView(context).apply {
                // 카카오맵 v2는 start()를 호출하면서 콜백을 달아주어야 지도가 뜹니다.
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            Log.d("KakaoMap", "지도 소멸됨")
                        }

                        override fun onMapError(error: Exception?) {
                            Log.e("KakaoMap", "지도 에러 발생: ${error?.message}")
                        }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            Log.d("KakaoMap", "지도 로딩 성공!")
                            // 지도가 완전히 불려오면 이 부분이 실행됩니다.
                            // 나중에 여기에 마커를 찍거나 카메라 이동 코드를 넣습니다.
                        }
                    }
                )
            }
        }
    )
}
