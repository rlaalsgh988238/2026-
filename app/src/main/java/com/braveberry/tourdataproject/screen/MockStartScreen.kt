package com.braveberry.tourdataproject.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import java.time.LocalDate

@Composable
fun MockStartScreen(
    sharedViewModel: PlanSharedViewModel,
    onNavigateToMakeCourse: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {

             sharedViewModel.onIntent(PlanSharedIntent.OnCitySelected("서울"))

                // 2. 가짜 날짜 강제 주입 (오늘부터 모레까지 2박 3일)
                sharedViewModel.onIntent(
                    PlanSharedIntent.OnDateSelected(
                        startDate = LocalDate.now(),
                        endDate = LocalDate.now().plusDays(2)
                    )
                )

                // 3. 내가 만든 메인 코스 화면으로 쏴버리기
                onNavigateToMakeCourse()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "🚀 작성한 기능 테스트 시작하기")
        }
    }
}