package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.braveberry.tourdataproject.ui.theme.TextMint
import com.tourdataproject.presentation.viewmodel.course.MakeCourseViewModel
import com.tourdataproject.presentation.viewmodel.course.uiState.CourseEffect
import com.tourdataproject.presentation.viewmodel.course.uiState.CourseEvent
import com.tourdataproject.presentation.viewmodel.course.uiState.CourseState
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel


data class MakeCourseUiState(
    val isLoading: Boolean = true,
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
    val category: String?
)



fun CourseState.toMakeCourseState(): MakeCourseUiState {
    try {
        val course = this.course
        //TODO: 에러 처리 생각 해야함 이거
        val tempCourseName = course?.courseName ?: ""
        val tempDatePeriod = course?.datePeriod ?: ""
        val tempDayPlans = course?.dayPlans?.map { dayPlan ->
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
                        category = schedule.category
                    )
                }
            )
        } ?: emptyList()

        return MakeCourseUiState(
            isLoading = this.isLoading,
            courseName = tempCourseName,
            datePeriod = tempDatePeriod,
            dayPlans = tempDayPlans
        )
    } catch (e: Exception) {
        android.util.Log.e("CrashCatch", "🚨 매퍼에서 크래시 발생: ${e.message}", e)
        return MakeCourseUiState(isLoading = false)
    }
}

@Composable
fun MakeCourseRoute(
    sharedViewModel: PlanSharedViewModel,
    makeCourseViewModel: MakeCourseViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddSchedule: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val sharedCourseState by sharedViewModel.courseState.collectAsState()
    val courseState by makeCourseViewModel.state.collectAsState()
    val uiState = courseState.toMakeCourseState()

    LaunchedEffect(sharedCourseState) {
        makeCourseViewModel.setInitialCourse(sharedCourseState)
    }

    LaunchedEffect(makeCourseViewModel.effect) {
        makeCourseViewModel.effect.collect { effect ->
            when (effect) {
                is CourseEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is CourseEffect.NavigateToAddSchedule -> onNavigateToAddSchedule()
                is CourseEffect.ShowToast -> onShowToast(effect.message)
                is CourseEffect.NavigateToCourseInfo -> { /* TODO */ }
                is CourseEffect.ShareCourse -> { /* TODO */ }
                is CourseEffect.NavigateToMapScreen -> { /* TODO */ }
                is CourseEffect.NavigateToHomeScreen -> { /* TODO */ }
            }
        }
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00B493))
        }
    } else {
        MakeCourseScreen(
            state = uiState,
            onEvent = makeCourseViewModel::onEvent,
            onFinalSaveClick = {
                makeCourseViewModel.onEvent(CourseEvent.OnSaveButtonClicked(sharedCourseState))
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeCourseScreen(
    state: MakeCourseUiState,
    onEvent: (CourseEvent) -> Unit,
    onFinalSaveClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            MakeCourseTopBar(
                courseName = state.courseName,
                datePeriod = state.datePeriod,
                onBackClick = { onEvent(CourseEvent.OnBackButtonClicked) }
            )
        },
        bottomBar = {
            // 🌟 하단 큼지막한 [저장] 버튼 영역 추가
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = onFinalSaveClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextMint // 기획안의 청록색 포인트 컬러
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "저장",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Surface(
                    onClick = { /* TODO: 숙소 추가 로직 필요 시 Event 추가 */ },
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "숙소 추가",
                            modifier = Modifier.size(16.dp),
                            tint = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "숙소", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(state.dayPlans) { dayPlan ->
                    DayPlanItem(
                        dayPlan = dayPlan,
                        onAddScheduleClick = { onEvent(CourseEvent.OnAddScheduleClicked(dayPlan.dayNumber)) }
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
    onBackClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = courseName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "이름 수정",
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                }
                Text(text = datePeriod, fontSize = 12.sp, color = Color.Gray)
            }
        }
        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
    }
}

@Composable
fun DayPlanItem(
    dayPlan: MakeCourseDayPlanState,
    onAddScheduleClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. N일차 날짜 헤더
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color(0xFFD0F0EA),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = dayPlan.dayLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMint,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = dayPlan.dateLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🌟 2. 추가된 일정이 있다면 리스트 쫙 그려주기
        dayPlan.schedules.forEach { schedule ->
            ScheduleItemView(schedule = schedule)
            Spacer(modifier = Modifier.height(12.dp)) // 일정 간 간격
        }

        // 3. '일정 추가' 버튼
        OutlinedButton(
            // 🌟 여기서 넘겨준 dayNumber(N일차)가 뷰모델을 거쳐 SharedViewModel의 currentAddingDayNumber가 됩니다!
            onClick = onAddScheduleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF00B493)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White
            )
        ) {
            Text(
                text = "일정 추가",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4A4A4A)
            )
        }
    }
}

@Composable
fun ScheduleItemView(schedule: MakeCourseScheduleState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // 아이템 간 상하 여백
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 좌측 순서 번호 동그라미
        Surface(
            shape = CircleShape,
            color = Color(0xFF00B493),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = schedule.order.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. 우측 장소 정보 카드 (청록색 테두리)
        Surface(
            modifier = Modifier.weight(1f), // 남은 가로 영역 꽉 채우기
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF00B493)), // 🌟 테두리 적용
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 장소 이름 및 메모
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.placeName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    if (schedule.memo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = schedule.memo,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 3. 우측 접근성 아이콘 (디자인 시안의 녹색/노란색 휠체어 아이콘)
                // TODO: 실제 데이터(category 등)에 따라 색상을 분기하도록 추후 수정 가능합니다.
                val iconColor =
                    if (schedule.order % 2 == 1) Color(0xFF70AD47) else Color(0xFFFF9900)
                Surface(
                    shape = CircleShape,
                    color = iconColor,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person, //TODO : 아이콘 교체
                            contentDescription = "접근성 아이콘",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
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
        MakeCourseDayPlanState(dayLabel = "1일차", dateLabel = "8/30", dayNumber = 1),
        MakeCourseDayPlanState(dayLabel = "2일차", dateLabel = "8/31", dayNumber = 2),
        MakeCourseDayPlanState(dayLabel = "3일차", dateLabel = "9/01", dayNumber = 3)
    )

    val mockState = MakeCourseUiState(
        isLoading = false,
        courseName = "거제 여행",
        datePeriod = "2026.08.30 ~ 2026.09.01",
        dayPlans = mockDayPlans
    )

    MakeCourseScreen(
        state = mockState,
        onEvent = {},
        onFinalSaveClick = {} // 프리뷰용 빈 람다 추가
    )
}