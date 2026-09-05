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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.braveberry.tourdataproject.R
import com.braveberry.tourdataproject.screen.pop.LoadingPopUp
import com.braveberry.tourdataproject.ui.theme.*
import com.tourdataproject.presentation.model.course.AccessibilityInfoUiModel
import com.tourdataproject.presentation.model.course.AccessibilityStatusUiModel
import com.tourdataproject.presentation.model.course.TravelCoursePresentationModel
import com.tourdataproject.presentation.utility.Log
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
    onNavigateToAddSchedule: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToEditSchedule: (Int) -> Unit,
    onShowToast: (String) -> Unit
) {
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
                    onNavigateToAddSchedule()
                }
                is CourseEffect.ShowToast -> onShowToast(effect.message)
                is CourseEffect.NavigateToCourseInfo -> { /* TODO */ }
                is CourseEffect.ShareCourse -> { /* TODO */ }
                is CourseEffect.NavigateToMapScreen -> { /* TODO */ }
                is CourseEffect.NavigateToEditSchedule -> onNavigateToEditSchedule(effect.dayNumber)
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

    if (sharedState.isLoading) {
        LoadingPopUp(message = "일정 정보를 가져오고 있습니다")
    } else if (sharedState.isSaving) {
        LoadingPopUp(message = "코스를 저장하고 있습니다")
    } else if (uiState.isError) {
        LaunchedEffect(uiState.errorMessage) {
            Log.e("MakeCourseDebug", "🚨 에러 발생: ${uiState.errorMessage}")
            onShowToast(uiState.errorMessage ?: "오류가 발생했습니다.")
            onNavigateBack()
        }
    } else {
        MakeCourseScreen(
            state = uiState,
            onIntent = makeCourseViewModel::onIntent,
            onSharedIntent = sharedViewModel::onIntent,
            onFinalSaveClick = {
                sharedViewModel.onIntent(PlanSharedIntent.OnSaveCourse)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeCourseScreen(
    state: MakeCourseUiState,
    onIntent: (CourseIntent) -> Unit,
    onSharedIntent: (PlanSharedIntent) -> Unit,
    onFinalSaveClick: () -> Unit
) {
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
                onBackClick = { onIntent(CourseIntent.OnBackButtonClicked) }
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
                    modifier = Modifier.fillMaxWidth().height(56.dp)
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
            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
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
fun MakeCourseTopBar(courseName: String, datePeriod: String, onBackClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = courseName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "이름 수정", modifier = Modifier.size(14.dp), tint = Color.Gray)
                }
                Text(text = datePeriod, fontSize = 15.sp, color = Color.Gray)
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
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
        OutlinedButton(
            onClick = onAddScheduleClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
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
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = Mint100, modifier = Modifier.size(28.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = schedule.order.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Mint100), color = Color.White) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = schedule.placeName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    if (schedule.memo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = schedule.memo, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                val iconColor = when (schedule.accessibilityInfo?.status) {
                    AccessibilityStatusUiModel.GOOD -> Green
                    AccessibilityStatusUiModel.WARNING -> Yellow
                    AccessibilityStatusUiModel.BAD -> Red
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun MakeCourseScreenPreview() {
    val mockDayPlans = listOf(
        MakeCourseDayPlanState(
            dayLabel = "1일차",
            dateLabel = "8/30",
            dayNumber = 1,
            schedules = listOf(
                MakeCourseScheduleState(
                    scheduleId = "1",
                    placeName = "해운대 해수욕장",
                    order = 1,
                    memo = "바다 구경",
                    category = "관광지",
                    accessibilityInfo = AccessibilityInfoUiModel(status = AccessibilityStatusUiModel.GOOD)
                )
            )
        ),
        MakeCourseDayPlanState(
            dayLabel = "2일차",
            dateLabel = "8/31",
            dayNumber = 2,
            schedules = emptyList()
        )
    )

    val mockState = MakeCourseUiState(
        isError = false,
        errorMessage = null,
        courseName = "부산 여행",
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
    val schedules: List<MakeCourseScheduleState> = emptyList()
)

data class MakeCourseScheduleState(
    val scheduleId: String,
    val placeName: String,
    val order: Int,
    val memo: String,
    val category: String?,
    val accessibilityInfo: AccessibilityInfoUiModel? = null
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
                }
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