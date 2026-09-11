package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.braveberry.tourdataproject.R
import com.braveberry.tourdataproject.ui.theme.*
import com.tourdataproject.presentation.model.plan.AccessibilityInfoPresentationModel
import com.tourdataproject.presentation.model.plan.AccessibilityStatusPresentationModel
import com.tourdataproject.presentation.model.plan.TravelCoursePresentationModel
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.utility.ScreenPurpose
import com.tourdataproject.presentation.viewmodel.course.MakeCourseViewModel
import com.tourdataproject.presentation.viewmodel.course.makeCourse.uiState.CourseEffect
import com.tourdataproject.presentation.viewmodel.course.makeCourse.uiState.CourseIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedEffect
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel

@Composable
fun MakeCourseRoute(
    sharedViewModel: PlanSharedViewModel,
    makeCourseViewModel: MakeCourseViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddSchedule: (String) -> Unit,
    onNavigateToAddStay: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToEditSchedule: (Int, String) -> Unit,
    onNavigateToFullMap: () -> Unit, // 추가
    onShowToast: (String) -> Unit = {}
)  {
    val sharedState by sharedViewModel.sharedState.collectAsState()

    val uiState = remember(sharedState.course) {
        sharedState.course.toMakeCourseState()
    }

    LaunchedEffect(makeCourseViewModel.effect) {
        makeCourseViewModel.effect.collect { effect ->
            when (effect) {
                is CourseEffect.NavigateBack -> onNavigateBack()
                is CourseEffect.NavigateToAddSchedule -> {
                    sharedViewModel.onIntent(PlanSharedIntent.OnSetAddingDayNumber(effect.dayNumber))
                    onNavigateToAddSchedule(ScreenPurpose.ADD_SCHEDULE)
                }
                is CourseEffect.ShowToast -> onShowToast(effect.message)
                is CourseEffect.NavigateToCourseInfo -> { /* TODO */ }
                is CourseEffect.ShareCourse -> { /* TODO */ }
                is CourseEffect.NavigateToMapScreen -> { /* TODO */ }
                is CourseEffect.NavigateToEditSchedule -> onNavigateToEditSchedule(effect.dayNumber,
                    ScreenPurpose.ADD_SCHEDULE)
                is CourseEffect.NavigateToAddStay -> onNavigateToAddStay(ScreenPurpose.ADD_STAY)
                is CourseEffect.NavigateToFullMap -> onNavigateToFullMap()
            }
        }
    }

    LaunchedEffect(sharedViewModel.effect) {
        sharedViewModel.effect.collect { effect ->
            when (effect) {
                is PlanSharedEffect.NavigateToHomeScreen -> onNavigateToHome()
                is PlanSharedEffect.ShowToast -> onShowToast(effect.message)
            }
        }
    }

    MakeCourseScreen(
        state = uiState,
        onIntent = makeCourseViewModel::onIntent,
        onSharedIntent = sharedViewModel::onIntent,
        onFinalSaveClick = {
            sharedViewModel.onIntent(PlanSharedIntent.OnSaveCourse)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeCourseScreen(
    state: MakeCourseUiState,
    onIntent: (CourseIntent) -> Unit,
    onSharedIntent: (PlanSharedIntent) -> Unit,
    onFinalSaveClick: () -> Unit
) {
    var showInfoDialog by remember { mutableStateOf(false) }


    if (showInfoDialog) {
        AccessibilityInfoDialog(
            onDismiss = { showInfoDialog = false }
        )
    }
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        containerColor = Color.White,
        topBar = {
            MakeCourseTopBar(
                courseName = state.courseName,
                datePeriod = state.datePeriod,
                onBackClick = { onIntent(CourseIntent.OnBackButtonClicked) },
                onInfoClick = { showInfoDialog = true }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onFinalSaveClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Mint100),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("저장", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 숙소 버튼 추가

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onIntent(CourseIntent.OnAddStayButtonClicked) },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF5F5F5)),
                            border = BorderStroke(1.dp, Color.Gray),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                        ) {
                            Text(text = "+ 숙소", fontSize = 14.sp, color = Color.Black)
                        }

                        OutlinedButton(
                            onClick = { onIntent(CourseIntent.OnViewFullMapButtonClicked) },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Mint100),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.green_map), // 지도 아이콘 리소스
                                    contentDescription = null,
                                    tint = Mint100,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("전체일정 지도 보기", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Mint100)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight, // 없으면 KeyboardArrowRight로 대체
                                    contentDescription = null,
                                    tint = Mint100,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }


                items(state.dayPlans) { dayPlan ->
                    DayPlanItem(
                        dayPlan = dayPlan,
                        onAddScheduleClick = { onIntent(CourseIntent.OnAddScheduleClicked(dayPlan.dayNumber)) },
                        onEditClick = { onIntent(CourseIntent.OnEditScheduleButtonClicked(dayPlan.dayNumber)) }
                    )
                }
            }
        }
    }
}

@Composable
fun MakeCourseTopBar(
    courseName: String,
    datePeriod: String,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // 아이콘이 커지므로 높이를 약간 여유 있게(56->64) 조정해도 좋습니다.
                .background(Color.White)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(com.braveberry.tourdataproject.R.drawable.arrow_circle_left),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.fillMaxSize() // 버튼 영역에 꽉 채움
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = courseName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = datePeriod, fontSize = 15.sp, color = Color.Gray)
            }
            // 인포 아이콘 크기 확대 적용
            IconButton(
                onClick = { /* 기능 추가 불필요 */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "정보",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp) // 여기서 크기를 결정합니다.
                )
            }
        }
    }
}


@Composable
fun DayPlanItem(dayPlan: MakeCourseDayPlanState, onAddScheduleClick: () -> Unit, onEditClick: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Mint20, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(end = 8.dp)) {
                Text(text = dayPlan.dayLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Mint100, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
            Text(text = dayPlan.dateLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "편집", fontSize = 14.sp, color = Color.Gray, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable(onClick = onEditClick).padding(4.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        dayPlan.schedules.forEach { schedule ->
            ScheduleItemView(schedule = schedule)
            Spacer(modifier = Modifier.height(12.dp))
        }
        // 숙소가 지정된 day면 스케줄 목록 다음, 일정추가 버튼 앞에 표시
        dayPlan.stay?.let { stay ->
            StayItemView(stay = stay)
            Spacer(modifier = Modifier.height(12.dp))
        }
        OutlinedButton(
            onClick = onAddScheduleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF00B493)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Text(text = "일정 추가", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4A4A4A))
        }
    }
}

@Composable
fun ScheduleItemView(schedule: MakeCourseScheduleState) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = Mint100, modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = schedule.order.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Mint100), color = Color.White) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = schedule.placeName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    if (schedule.memo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = schedule.memo, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                val iconColor = when (schedule.accessibilityInfo?.status) {
                    AccessibilityStatusPresentationModel.GOOD -> Green
                    AccessibilityStatusPresentationModel.WARNING -> Yellow
                    AccessibilityStatusPresentationModel.BAD -> Red
                    else -> Color.Gray
                }
                Surface(shape = CircleShape, color = iconColor, modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = ImageVector.vectorResource(id = R.drawable.accessible), contentDescription = "접근성 아이콘", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StayItemView(stay: MakeCourseScheduleState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 배경 Surface를 제거하고, SVG 리소스 자체를 그대로 표시합니다.
        // tint를 주면 SVG 내부의 노란색이 덮여버리므로 tint = Color.Unspecified가 핵심입니다.
        Icon(
            painter = painterResource(id = R.drawable.stay_icon),
            contentDescription = "숙소 마커",
            tint = Color.Unspecified, // SVG 내부의 노란색과 흰색을 그대로 유지
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 우측 정보 카드는 기존과 동일
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Mint100),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "숙소", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stay.placeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // ... (접근성 아이콘 부분은 기존 코드 유지)
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun MakeCourseScreenPreview() {
    val mockDayPlans = listOf(
        MakeCourseDayPlanState(
            dayLabel = "Day 1",
            dateLabel = "8/30 (일)",
            dayNumber = 1,
            schedules = listOf(
                MakeCourseScheduleState(
                    scheduleId = "1",
                    placeName = "가덕휴게소",
                    order = 1,
                    memo = "메모",
                    category = "관광지",
                    accessibilityInfo = AccessibilityInfoPresentationModel(status = AccessibilityStatusPresentationModel.GOOD)
                ),
                MakeCourseScheduleState(
                    scheduleId = "2",
                    placeName = "매미성",
                    order = 2,
                    memo = "",
                    category = "관광지",
                    accessibilityInfo = AccessibilityInfoPresentationModel(status = AccessibilityStatusPresentationModel.WARNING)
                )
            ),
            stay = MakeCourseScheduleState(
                scheduleId = "stay1",
                placeName = "거제 YAHO HOTEL",
                order = 0,
                memo = "",
                category = "숙소",
                accessibilityInfo = AccessibilityInfoPresentationModel(status = AccessibilityStatusPresentationModel.WARNING)
            )
        ),
        MakeCourseDayPlanState(
            dayLabel = "Day 2",
            dateLabel = "8/31 (월)",
            dayNumber = 2,
            schedules = emptyList()
        )
    )

    val mockState = MakeCourseUiState(
        isError = false,
        errorMessage = null,
        courseName = "거제 여행",
        datePeriod = "2026.08.30 ~ 2026.08.31",
        dayPlans = mockDayPlans
    )

    MakeCourseScreen(
        state = mockState,
        onIntent = {},
        onSharedIntent = {},
        onFinalSaveClick = {}
    )
}



@Composable
fun AccessibilityInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // --- 타이틀 및 닫기 버튼 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "정보",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "화장실 접근성 색 기준 안내",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- 설명 텍스트 ---
                Text(
                    text = "주변 화장실 개수와 이동 시간을 종합적으로 분석하여 산출한 점수에 따라 색상이 구분됩니다.",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- 3가지 상태 카드 영역 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    StatusCard(
                        modifier = Modifier.weight(1f),
                        title = "안전",
                        scoreText = "80점 이상",
                        backgroundColor = Green.copy(alpha = 0.2f),
                        iconColor = Green
                    )

                    StatusCard(
                        modifier = Modifier.weight(1f),
                        title = "주의",
                        scoreText = "50~79점",
                        backgroundColor = Yellow.copy(alpha = 0.2f),
                        iconColor = Yellow
                    )

                    StatusCard(
                        modifier = Modifier.weight(1f),
                        title = "위험",
                        scoreText = "50점 미만",
                        backgroundColor = Red.copy(alpha = 0.2f),
                        iconColor = Red
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    modifier: Modifier = Modifier,
    title: String,
    scoreText: String,
    backgroundColor: Color,
    iconColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = CircleShape,
                color = iconColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.accessible), // 접근성 아이콘 (기존꺼 재사용)
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = scoreText,
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    }
}


// --- Mapper ---
data class MakeCourseUiState(
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val courseName: String = "",
    val datePeriod: String = "",
    val dayPlans: List<MakeCourseDayPlanState> = emptyList()
)

data class MakeCourseDayPlanState(
    val dayLabel: String = "",
    val dateLabel: String = "",
    val dayNumber: Int = 0,
    val schedules: List<MakeCourseScheduleState> = emptyList(),
    val stay: MakeCourseScheduleState? = null
)

data class MakeCourseScheduleState(
    val scheduleId: String,
    val placeName: String,
    val order: Int,
    val memo: String,
    val category: String?,
    val accessibilityInfo: AccessibilityInfoPresentationModel? = null
)

fun TravelCoursePresentationModel.toMakeCourseState(): MakeCourseUiState {
    try {
        if (this.courseName.isBlank() || this.datePeriod.isBlank()) {
            return MakeCourseUiState(
                isError = true,
                errorMessage = "코스 기본 정보(이름, 날짜)가 누락되었습니다."
            )
        }

        val tempDayPlans = this.dayPlans.map { dayPlan ->
            // stay는 기본값(scheduleId="")일 수 있으므로 실제로 지정된 경우에만 표시
            val stayState = if (dayPlan.stay.scheduleId.isNotBlank()) {
                MakeCourseScheduleState(
                    scheduleId = dayPlan.stay.scheduleId,
                    placeName = dayPlan.stay.scheduleName,
                    order = dayPlan.stay.order,
                    memo = dayPlan.stay.memo,
                    category = dayPlan.stay.category,
                    accessibilityInfo = dayPlan.stay.accessibilityInfo
                )
            } else {
                null
            }

            MakeCourseDayPlanState(
                dayLabel = dayPlan.dayLabel,
                dateLabel = dayPlan.dateLabel,
                dayNumber = dayPlan.rawDayNumber,
                schedules = dayPlan.schedules.map { schedule ->
                    MakeCourseScheduleState(
                        scheduleId = schedule.scheduleId,
                        placeName = schedule.scheduleName,
                        order = schedule.order,
                        memo = schedule.memo,
                        category = schedule.category,
                        accessibilityInfo = schedule.accessibilityInfo
                    )
                },
                stay = stayState
            )
        }

        return MakeCourseUiState(
            isError = false,
            courseName = this.courseName,
            datePeriod = this.datePeriod,
            dayPlans = tempDayPlans
        )
    } catch (e: Exception) {
        Log.e("CrashCatch", "🚨 매퍼에서 크래시 발생: ${e.message}", e)
        return MakeCourseUiState(
            isError = true,
            errorMessage = "데이터를 처리하는 중 문제가 발생했습니다."
        )
    }
}
