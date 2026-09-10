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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.ui.theme.DisabledGray
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.braveberry.tourdataproject.ui.theme.WeekendBlue
import com.tourdataproject.presentation.utility.ScreenPurpose
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.DateSelectionViewModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.CalendarDayPresentationModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.CalendarMonthPresentationModel
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionIntent
import com.tourdataproject.presentation.viewmodel.plan.dateSelect.uiState.DateSelectionState
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DateSelectionRoute(
    sharedViewModel: PlanSharedViewModel = hiltViewModel(),
    viewModel: DateSelectionViewModel = hiltViewModel(),
    onNavigateToNext: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()

    val isStayMode = state.purpose == ScreenPurpose.ADD_STAY
    val stayName = sharedState.draftStay?.scheduleName ?: ""

    LaunchedEffect(Unit) {
        viewModel.effect.collect { currentEffect ->
            when (currentEffect) {
                is DateSelectionEffect.NavigateToNextScreen -> {
                    if (isStayMode) {
                        sharedViewModel.onIntent(PlanSharedIntent.OnConfirmStaySelection)
                    } else {
                        sharedViewModel.onIntent(PlanSharedIntent.OnConfirmDateSelection)
                    }
                    onNavigateToNext()
                }
                is DateSelectionEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    // 숙소 모드일 때 선택 가능한 날짜 범위 (코스 전체 여행 기간)
    val courseStartDate = if (isStayMode) sharedState.course.rawStartDate.takeIf { it != 0L }?.toLocalDate() else null
    val courseEndDate = if (isStayMode) sharedState.course.rawEndDate.takeIf { it != 0L }?.toLocalDate() else null

    // 숙소 모드에서는 course 날짜를 임시 선택값으로 사용하지 않음 (draft만 사용)
    val currentStartMillis: Long?
    val currentEndMillis: Long?
    if (isStayMode) {
        currentStartMillis = sharedState.draftStartDate
        currentEndMillis = sharedState.draftEndDate
    } else {
        val isDraftActive = sharedState.draftStartDate != null || sharedState.draftEndDate != null
        currentStartMillis = if (isDraftActive) {
            sharedState.draftStartDate
        } else {
            sharedState.course.rawStartDate.takeIf { it != 0L }
        }
        currentEndMillis = if (isDraftActive) {
            sharedState.draftEndDate
        } else {
            sharedState.course.rawEndDate.takeIf { it != 0L }
        }
    }

    // 숙소 모드에서는 코스 기간에 해당하는 달만 보여줌, 일반 모드는 뷰모델이 관리하는 targetMonths 사용
    val displayMonths = if (isStayMode && courseStartDate != null && courseEndDate != null) {
        monthRange(courseStartDate, courseEndDate)
    } else {
        state.targetMonths
    }

    val calendarMonths by remember(displayMonths, currentStartMillis, currentEndMillis, courseStartDate, courseEndDate) {
        derivedStateOf {
            viewModel.generateCalendarMonths(
                yearMonths = displayMonths,
                startMillis = currentStartMillis,
                endMillis = currentEndMillis,
                minSelectableDate = courseStartDate,
                maxSelectableDate = courseEndDate
            )
        }
    }

    val isNextEnabled = currentStartMillis != null && currentEndMillis != null

    val selectedRangeLabel = if (isStayMode && currentStartMillis != null && currentEndMillis != null) {
        val start = currentStartMillis.toLocalDate()
        val end = currentEndMillis.toLocalDate()
        "${formatDateWithDay(start)} - ${formatDateWithDay(end)} 선택"
    } else {
        null
    }

    DateSelectionScreen(
        state = state,
        calendarMonths = calendarMonths,
        isNextEnabled = isNextEnabled,
        isStayMode = isStayMode,
        stayName = stayName,
        selectedRangeLabel = selectedRangeLabel,
        onIntent = viewModel::onIntent,
        onSharedIntent = sharedViewModel::onIntent
    )
}

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun monthRange(start: LocalDate, end: LocalDate): List<YearMonth> {
    val startMonth = YearMonth.from(start)
    val endMonth = YearMonth.from(end)
    return generateSequence(startMonth) { it.plusMonths(1) }
        .takeWhile { !it.isAfter(endMonth) }
        .toList()
}

private fun formatDateWithDay(date: LocalDate): String {
    val dayOfWeekKorean = when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> "일"
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
    }
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return "${date.format(formatter)}(${dayOfWeekKorean})"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionScreen(
    state: DateSelectionState,
    calendarMonths: List<CalendarMonthPresentationModel>,
    isNextEnabled: Boolean,
    isStayMode: Boolean = false,
    stayName: String = "",
    selectedRangeLabel: String? = null,
    onIntent: (DateSelectionIntent) -> Unit,
    onSharedIntent: (PlanSharedIntent) -> Unit
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }
    // 숙소 모드는 코스 기간으로 달이 고정되어 있어서 추가로 불러올 필요 없음
    LaunchedEffect(shouldLoadMore, isStayMode) {
        if (shouldLoadMore && !isStayMode) onIntent(DateSelectionIntent.OnLoadMoreMonths)
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isStayMode) "숙소 체크인-체크아웃 선택" else "날짜 선택",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(DateSelectionIntent.OnBackButtonClicked) }) {
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
                    val buttonText = if (isStayMode) {
                        selectedRangeLabel ?: "체크인-체크아웃 날짜를 선택해주세요"
                    } else {
                        "다음"
                    }
                    Button(
                        onClick = { onIntent(DateSelectionIntent.OnNextButtonClicked) },
                        enabled = isNextEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            disabledContainerColor = DisabledGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                if (isStayMode) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
                    ) {
                        Text(
                            text = stayName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "체크인-체크아웃 날짜를 선택해주세요.",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = "언제 떠나시나요?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 32.dp)
                    )
                }
            }
            items(calendarMonths) { monthModel ->
                CalendarMonthView(
                    month = monthModel,
                    onDateSelected = { date ->
                        onSharedIntent(PlanSharedIntent.OnCalendarDateTapped(date))
                    }
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun CalendarMonthView(
    month: CalendarMonthPresentationModel,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = month.title,
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

        month.weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { dayModel ->
                    val date = dayModel.date
                    if (date != null) {
                        DateCell(
                            day = dayModel.dayNumber,
                            isStart = dayModel.isStart,
                            isEnd = dayModel.isEnd,
                            isInRange = dayModel.isInRange,
                            isWeekend = dayModel.isWeekend,
                            isPast = dayModel.isPast,
                            onClick = {
                                if (!dayModel.isPast) onDateSelected(date)
                            },
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
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.2f)
            .then(if (!isPast) Modifier.clickable(onClick = onClick) else Modifier),
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
                isPast -> Color.LightGray
                isWeekend -> WeekendBlue
                else -> Color.Black
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DateSelectionScreenPreview() {
    val dummyMonth = CalendarMonthPresentationModel(
        yearMonth = YearMonth.now(),
        title = "2026년 9월",
        weeks = listOf(
            listOf(
                CalendarDayPresentationModel(date = null, dayNumber = 0),
                CalendarDayPresentationModel(date = null, dayNumber = 0),
                CalendarDayPresentationModel(date = LocalDate.now(), dayNumber = 1, isStart = true),
                CalendarDayPresentationModel(date = LocalDate.now().plusDays(1), dayNumber = 2, isInRange = true),
                CalendarDayPresentationModel(date = LocalDate.now().plusDays(2), dayNumber = 3, isEnd = true),
                CalendarDayPresentationModel(date = LocalDate.now().plusDays(3), dayNumber = 4),
                CalendarDayPresentationModel(date = LocalDate.now().plusDays(4), dayNumber = 5, isWeekend = true)
            )
        )
    )
    DateSelectionScreen(
        state = DateSelectionState(),
        calendarMonths = listOf(dummyMonth),
        isNextEnabled = true,
        isStayMode = true,
        stayName = "거제 YAHO HOTEL",
        selectedRangeLabel = "2026.08.30(일) - 2026.08.31(월) 선택",
        onIntent = {},
        onSharedIntent = {}
    )
}
