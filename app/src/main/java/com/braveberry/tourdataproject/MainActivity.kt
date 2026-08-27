package com.braveberry.tourdataproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.braveberry.tourdataproject.screen.plan.DateSelectionRoute
import com.braveberry.tourdataproject.ui.theme.TourDataProjectTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TourDataProjectTheme {
                // 기존 지도 화면 대신 날짜 선택 화면을 바로 띄웁니다.
                DateSelectionRoute(
                    onNavigateToNext = {
                        // TODO: 다음 화면(일정 추가 또는 기본 플랜 화면)으로 이동하는 네비게이션 로직
                    },
                    onNavigateBack = {
                        // TODO: 이전 화면으로 돌아가거나 앱을 종료하는 로직
                        finish()
                    }
                )
            }
            /**/
        }
    }
}
