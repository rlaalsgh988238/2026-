package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.braveberry.tourdataproject.ui.theme.DisabledGray
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.braveberry.tourdataproject.ui.theme.WeekendBlue
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.DateSelectionViewModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionEvent
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionState
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DateSelectionRoute(
    sharedViewModel: PlanSharedViewModel,
    viewModel: DateSelectionViewModel = hiltViewModel(),
    onNavigateToNext: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val effect = viewModel.effect

    LaunchedEffect(effect) {
        effect.collect { currentEffect ->
            when (currentEffect) {
                is DateSelectionEffect.NavigateToNextScreen -> {
                    state.selectedDate?.let {
                        //TODO 공유 뷰모델에 선택한 날짜 저장
                    }
                    onNavigateToNext()
                }
                is DateSelectionEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    DateSelectionScreen(state = state, onEvent = viewModel::setEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionScreen(
    state: DateSelectionState,
    onEvent: (DateSelectionEvent) -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "날짜 선택",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(DateSelectionEvent.OnBackButtonClicked) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            // 🌟 하단 네비게이션 바 가림 방지 적용
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // 시스템 바 영역 확보
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { onEvent(DateSelectionEvent.OnNextButtonClicked) },
                        enabled = state.isNextButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            disabledContainerColor = DisabledGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "다음",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "언제 떠나시나요?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 달력 리스트
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(state.targetMonths) { yearMonth ->
                    CalendarMonthView(
                        yearMonth = yearMonth,
                        selectedDate = state.selectedDate,
                        onDateSelected = { onEvent(DateSelectionEvent.OnDateSelected(it)) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarMonthView(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val firstDayOfMonth = yearMonth.atDay(1)
    // 일(0)~토(6) 오프셋 계산
    val firstDayOffset = if (firstDayOfMonth.dayOfWeek.value == 7) 0 else firstDayOfMonth.dayOfWeek.value
    val daysInMonth = yearMonth.lengthOfMonth()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEachIndexed { index, day ->
                val textColor = if (index == 0 || index == 6) WeekendBlue else Color.Gray
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = textColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val totalCells = firstDayOffset + daysInMonth
        val rows = Math.ceil(totalCells / 7.0).toInt()

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstDayOffset + 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val currentDate = yearMonth.atDay(dayNumber)
                            val isSelected = currentDate == selectedDate
                            val isWeekend = col == 0 || col == 6

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) BorderStroke(1.dp, PrimaryTeal) else null,
                                color = if (isSelected) PrimaryTeal.copy(alpha = 0.1f) else Color.Transparent,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onDateSelected(currentDate) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> PrimaryTeal
                                            isWeekend -> WeekendBlue
                                            else -> Color.Black
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DateSelectionScreenPreview() {
    val mockState = DateSelectionState(
        selectedDate = LocalDate.now(),
        targetMonths = listOf(
            YearMonth.now(),
            YearMonth.now().plusMonths(1)
        )
    )

    DateSelectionScreen(
        state = mockState,
        onEvent = {}
    )
}
