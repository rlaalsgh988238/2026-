package com.braveberry.tourdataproject.screen.plan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.braveberry.tourdataproject.R
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
    onNavigateBack: () -> Unit,
    isEditMode: Boolean = false,
    editCourseId: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

    val isStayMode = !isEditMode &&
            state.purpose == ScreenPurpose.ADD_STAY

    val isSaving = isEditMode && sharedState.isSavingEdit
    val stayName = sharedState.draftStay?.scheduleName.orEmpty()

    val currentNavigateNext by rememberUpdatedState(onNavigateToNext)

    // 수정 화면의 날짜 선택값은 독립 ViewModel에서 초기화
    LaunchedEffect(
        isEditMode,
        editCourseId,
        sharedState.course.courseId,
        sharedState.isCourseLoading,
        sharedState.courseLoadError
    ) {
        val course = sharedState.course

        if (
            isEditMode &&
            !sharedState.isCourseLoading &&
            sharedState.courseLoadError == null &&
            course.courseId == editCourseId
        ) {
            viewModel.onIntent(
                DateSelectionIntent.OnInitializeEdit(
                    courseId = course.courseId,
                    startMillis = course.rawStartDate,
                    endMillis = course.rawEndDate
                )
            )
        }
    }

    // 오래 실행되는 effect 수집에서도 최신 상태와 콜백을 사용
    val handleEffect by rememberUpdatedState<(DateSelectionEffect) -> Unit>(
        newValue = { effect ->
            when (effect) {
                DateSelectionEffect.NavigateBack -> {
                    if (!sharedState.isSavingEdit) {
                        onNavigateBack()
                    }
                }

                DateSelectionEffect.NavigateToNextScreen -> {
                    if (isEditMode) {
                        val start = state.editStartMillis
                        val end = state.editEndMillis

                        if (
                            state.isEditInitialized &&
                            start != null &&
                            end != null
                        ) {
                            sharedViewModel.saveEditedCourseDates(
                                courseId = state.editCourseId,
                                startMillis = start,
                                endMillis = end
                            )
                        }
                    } else {
                        // 기존 생성·숙소 선택 처리 유지
                        if (isStayMode) {
                            sharedViewModel.onIntent(
                                PlanSharedIntent.OnConfirmStaySelection
                            )
                        } else {
                            sharedViewModel.onIntent(
                                PlanSharedIntent.OnConfirmDateSelection
                            )
                        }

                        onNavigateToNext()
                    }
                }
            }
        }
    )

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(
            Lifecycle.State.STARTED
        ) {
            viewModel.effect.collect { effect ->
                handleEffect(effect)
            }
        }
    }

    // DB 저장 완료가 확인된 경우에만 목록으로 이동
    LaunchedEffect(
        isEditMode,
        editCourseId,
        sharedState.savedEditCourseId
    ) {
        if (
            isEditMode &&
            editCourseId != null &&
            sharedState.savedEditCourseId == editCourseId
        ) {
            currentNavigateNext()
        }
    }

    BackHandler(enabled = isEditMode) {
        if (!isSaving) {
            viewModel.onIntent(
                DateSelectionIntent.OnBackButtonClicked
            )
        }
    }

    val courseStartDate = if (isStayMode) {
        sharedState.course.rawStartDate
            .takeIf { it != 0L }
            ?.toLocalDate()
    } else {
        null
    }

    val courseEndDate = if (isStayMode) {
        sharedState.course.rawEndDate
            .takeIf { it != 0L }
            ?.toLocalDate()
    } else {
        null
    }

    val currentStartMillis: Long?
    val currentEndMillis: Long?

    when {
        isEditMode -> {
            currentStartMillis = state.editStartMillis
            currentEndMillis = state.editEndMillis
        }

        isStayMode -> {
            currentStartMillis = sharedState.draftStartDate
            currentEndMillis = sharedState.draftEndDate
        }

        else -> {
            val isDraftActive =
                sharedState.draftStartDate != null ||
                        sharedState.draftEndDate != null

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
    }

    val displayMonths = if (
        isStayMode &&
        courseStartDate != null &&
        courseEndDate != null
    ) {
        monthRange(courseStartDate, courseEndDate)
    } else {
        state.targetMonths
    }

    val calendarMonths = remember(
        viewModel,
        displayMonths,
        currentStartMillis,
        currentEndMillis,
        courseStartDate,
        courseEndDate,
        isEditMode
    ) {
        viewModel.generateCalendarMonths(
            yearMonths = displayMonths,
            startMillis = currentStartMillis,
            endMillis = currentEndMillis,
            minSelectableDate = courseStartDate,
            maxSelectableDate = courseEndDate,
            allowPastDates = isEditMode
        )
    }

    val isEditReady = !isEditMode || (
            state.isEditInitialized &&
                    !sharedState.isCourseLoading &&
                    sharedState.courseLoadError == null
            )

    val isNextEnabled =
        isEditReady &&
                currentStartMillis != null &&
                currentEndMillis != null &&
                currentStartMillis <= currentEndMillis &&
                !isSaving &&
                (!isEditMode || sharedState.savedEditCourseId == null)

    val selectedRangeLabel = if (
        isStayMode &&
        currentStartMillis != null &&
        currentEndMillis != null
    ) {
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
        onSharedIntent = sharedViewModel::onIntent,
        isEditMode = isEditMode,
        isSaving = isSaving,
        errorMessage = if (isEditMode) {
            sharedState.editSaveError ?: sharedState.courseLoadError
        } else {
            null
        },
        onDateSelected = { date ->
            if (isEditMode) {
                if (isEditReady && !isSaving) {
                    viewModel.onIntent(
                        DateSelectionIntent.OnEditDateTapped(date)
                    )
                }
            } else {
                sharedViewModel.onIntent(
                    PlanSharedIntent.OnCalendarDateTapped(date)
                )
            }
        }
    )
}

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

private fun monthRange(
    start: LocalDate,
    end: LocalDate
): List<YearMonth> {
    val startMonth = YearMonth.from(start)
    val endMonth = YearMonth.from(end)

    return generateSequence(startMonth) {
        it.plusMonths(1)
    }.takeWhile {
        !it.isAfter(endMonth)
    }.toList()
}

private fun formatDateWithDay(date: LocalDate): String {
    val day = when (date.dayOfWeek) {
        DayOfWeek.SUNDAY -> "일"
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return "${date.format(formatter)}($day)"
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
    onSharedIntent: (PlanSharedIntent) -> Unit,
    isEditMode: Boolean = false,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onDateSelected: ((LocalDate) -> Unit)? = null
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo
                .visibleItemsInfo
                .lastOrNull()
                ?.index ?: 0

            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(
        shouldLoadMore,
        isStayMode,
        calendarMonths.size
    ) {
        if (shouldLoadMore && !isStayMode) {
            onIntent(DateSelectionIntent.OnLoadMoreMonths)
        }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when {
                                isEditMode -> "여행 날짜 변경"
                                isStayMode -> "숙소 체크인-체크아웃 선택"
                                else -> "날짜 선택"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            enabled = !isSaving,
                            onClick = {
                                onIntent(
                                    DateSelectionIntent.OnBackButtonClicked
                                )
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    R.drawable.arrow_circle_left
                                ),
                                contentDescription = "뒤로가기",
                                modifier = Modifier.size(40.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                HorizontalDivider(
                    color = Color(0xFFF0F0F0),
                    thickness = 1.dp
                )
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 16.dp
                        )
                ) {
                    errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    val buttonText = when {
                        isEditMode -> "저장"

                        isStayMode -> selectedRangeLabel
                            ?: "체크인-체크아웃 날짜를 선택해주세요"

                        else -> "다음"
                    }

                    Button(
                        onClick = {
                            onIntent(
                                DateSelectionIntent.OnNextButtonClicked
                            )
                        },
                        enabled = isNextEnabled && !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            disabledContainerColor = DisabledGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = buttonText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
                        modifier = Modifier.padding(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 32.dp
                        )
                    ) {
                        Text(
                            text = stayName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

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
                        modifier = Modifier.padding(
                            start = 20.dp,
                            end = 20.dp,
                            bottom = 32.dp
                        )
                    )
                }
            }

            items(
                items = calendarMonths,
                key = { it.yearMonth.toString() }
            ) { monthModel ->
                CalendarMonthView(
                    month = monthModel,
                    onDateSelected = { date ->
                        if (!isSaving) {
                            if (onDateSelected != null) {
                                onDateSelected(date)
                            } else {
                                onSharedIntent(
                                    PlanSharedIntent.OnCalendarDateTapped(date)
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun CalendarMonthView(
    month: CalendarMonthPresentationModel,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf(
        "일", "월", "화", "수", "목", "금", "토"
    )

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

        Row(Modifier.fillMaxWidth()) {
            daysOfWeek.forEachIndexed { index, day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = when (index) {
                        0, 6 -> WeekendBlue
                        else -> Color.Gray
                    },
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        month.weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
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
                                if (!dayModel.isPast) {
                                    onDateSelected(date)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
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
            .then(
                if (!isPast) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isInRange || isStart || isEnd) {
            val shape = when {
                isStart && isEnd -> RoundedCornerShape(8.dp)

                isStart -> RoundedCornerShape(
                    topStart = 8.dp,
                    bottomStart = 8.dp
                )

                isEnd -> RoundedCornerShape(
                    topEnd = 8.dp,
                    bottomEnd = 8.dp
                )

                else -> RoundedCornerShape(0.dp)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .background(
                        color = if (isStart || isEnd) {
                            PrimaryTeal
                        } else {
                            PrimaryTeal.copy(alpha = 0.15f)
                        },
                        shape = shape
                    )
            )
        }

        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isStart || isEnd) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            color = when {
                isStart || isEnd -> Color.White
                isPast -> Color.LightGray
                isWeekend -> WeekendBlue
                else -> Color.Black
            }
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
fun DateSelectionScreenPreview() {
    val yearMonth = YearMonth.of(2026, 9)
    val offset = yearMonth.atDay(1).dayOfWeek.value % 7
    val rows = (offset + yearMonth.lengthOfMonth() + 6) / 7

    val days = (0 until rows * 7).map { index ->
        val dayNumber = index - offset + 1

        if (dayNumber in 1..yearMonth.lengthOfMonth()) {
            CalendarDayPresentationModel(
                date = yearMonth.atDay(dayNumber),
                dayNumber = dayNumber,
                isStart = dayNumber == 20,
                isEnd = dayNumber == 23,
                isInRange = dayNumber in 21..22,
                isWeekend = index % 7 == 0 || index % 7 == 6
            )
        } else {
            CalendarDayPresentationModel(
                date = null,
                dayNumber = 0
            )
        }
    }

    DateSelectionScreen(
        state = DateSelectionState(),
        calendarMonths = listOf(
            CalendarMonthPresentationModel(
                yearMonth = yearMonth,
                title = "2026년 9월",
                weeks = days.chunked(7)
            )
        ),
        isNextEnabled = true,
        onIntent = {},
        onSharedIntent = {},
        isEditMode = true
    )
}
