package com.braveberry.tourdataproject.screen.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tourdataproject.presentation.viewmodel.splash.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onInitComplete: () -> Unit
) {
    val isReady by viewModel.isDatabaseReady.collectAsStateWithLifecycle()

    // 데이터베이스 준비가 완료되면 콜백 실행
    LaunchedEffect(isReady) {
        if (isReady) {
            onInitComplete()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 여기에 스플래시 로고나 로딩 인디케이터를 넣습니다.
        Text(text = "데이터를 준비 중입니다...")
    }
}