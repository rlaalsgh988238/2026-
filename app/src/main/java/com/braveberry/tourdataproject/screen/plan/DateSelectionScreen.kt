package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedEvent
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

    LaunchedEffect(Unit) {
        viewModel.effect.collect { currentEffect ->
            when (currentEffect) {
                is DateSelectionEffect.NavigateToNextScreen -> {
                    // 시작일/종료일이 모두 있을 때만 SharedViewModel에 전달
                    val start = state.startDate
                    val end = state.endDate
                    if (start != null && end != null) {
                        sharedViewModel.setEvent(
                            PlanSharedEvent.OnDateSelected(start, end)
                        )
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
    val listState = rememberLazyListState()

    // 무한 스크롤: 마지막에서 3번째 아이템이 보이면 더 불러오기
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onEvent(DateSelectionEvent.OnLoadMoreMonths)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("날짜 선택", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(DateSelectionEvent.OnBackButtonClicked) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
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
                        Text("다음", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Text(
                    text = "언제 떠나시나요?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
                )
            }

            items(state.targetMonths) { yearMonth ->
                CalendarMonthView(
                    yearMonth = yearMonth,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onDateSelected = { onEvent(DateSelectionEvent.OnDateSelected(it)) }
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun CalendarMonthView(
    yearMonth: YearMonth,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOffset = if (firstDayOfMonth.dayOfWeek.value == 7) 0 else firstDayOfMonth.dayOfWeek.value
    val daysInMonth = yearMonth.lengthOfMonth()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            textAlign = TextAlign.Center
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEachIndexed { index, day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = when (index) {
                        0 -> Color.Red.copy(alpha = 0.6f)
                        6 -> WeekendBlue
                        else -> Color.Gray
                    },
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val totalCells = firstDayOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - firstDayOffset + 1

                    if (dayNumber in 1..daysInMonth) {
                        val currentDate = yearMonth.atDay(dayNumber)
                        val isStart = currentDate == startDate
                        val isEnd = currentDate == endDate
                        val isInRange = startDate != null && endDate != null &&
                                currentDate.isAfter(startDate) && currentDate.isBefore(endDate)

                        DateCell(
                            day = dayNumber,
                            isStart = isStart,
                            isEnd = isEnd,
                            isInRange = isInRange,
                            isWeekend = (col == 0 || col == 6),
                            onClick = { onDateSelected(currentDate) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DateCell(
    day: Int,
    isStart: Boolean,
    isEnd: Boolean,
    isInRange: Boolean,
    isWeekend: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isInRange || isStart || isEnd) {
            val shape = when {
                isStart && isEnd -> RoundedCornerShape(8.dp)
                isStart -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                isEnd -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .background(
                        color = if (isStart || isEnd) PrimaryTeal else PrimaryTeal.copy(alpha = 0.15f),
                        shape = shape
                    )
            )
        }

        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isStart || isEnd) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isStart || isEnd -> Color.White
                isWeekend -> WeekendBlue
                else -> Color.Black
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DateSelectionScreenPreview() {
    val mockState = DateSelectionState(
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(3)
    )
    DateSelectionScreen(
        state = mockState,
        onEvent = {}
    )
}