package com.braveberry.tourdataproject.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.R
import com.tourdataproject.presentation.viewmodel.splash.SplashViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onInitComplete: () -> Unit
) {
    // 1. ViewModel 상태 구독 (역할 수행)
    val isReady by viewModel.isDatabaseReady.collectAsStateWithLifecycle()

    // 2. 데이터베이스 준비 완료 시 화면 전환 로직
    LaunchedEffect(isReady) {
        if (isReady) {
            onInitComplete()
        }
    }

    // 3. UI 렌더링
    SplashContent(message = "잠시만 기다려주세요")
}

@Composable
fun SplashContent(
    message: String
) {
    // 무한 회전 애니메이션 프로퍼티
    val infiniteTransition = rememberInfiniteTransition(label = "splash_loading")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 이미지의 로고 + 회전 화살표 재현 영역
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // 중앙 고정 앱 로고
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp)
                )

                // 리소스 없이 그리는 노란색 회전 호(Arc)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(angle) // 애니메이션 적용
                        .padding(10.dp)
                ) {
                    drawArc(
                        color = Color(0xFFF7CD18), // 이미지의 노란색
                        startAngle = 0f,
                        sweepAngle = 240f, // 화살표 몸통 길이
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 하단 메시지 프로퍼티
            Text(
                text = message,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    // 프리뷰에서도 메시지 프로퍼티를 전달하여 확인
    SplashContent(message = "잠시만 기다려주세요")
}
