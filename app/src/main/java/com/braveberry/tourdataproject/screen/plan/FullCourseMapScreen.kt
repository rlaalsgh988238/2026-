package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.R
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.tourdataproject.presentation.model.plan.DayPlanPresentationModel
import com.tourdataproject.presentation.model.plan.ScheduleItemPresentationModel
import com.tourdataproject.presentation.model.plan.TravelCoursePresentationModel
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun FullCourseMapRoute(
    sharedViewModel: PlanSharedViewModel,
    onNavigateBack: () -> Unit
) {
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()
    val dayPlans = sharedState.course.dayPlans

    var selectedDayNumber by remember(dayPlans) {
        mutableIntStateOf(dayPlans.firstOrNull()?.rawDayNumber ?: 1)
    }
    var selectedScheduleId by remember { mutableStateOf<String?>(null) }

    FullCourseMapScreen(
        course = sharedState.course,
        selectedDayNumber = selectedDayNumber,
        selectedScheduleId = selectedScheduleId,
        onDaySelected = { selectedDayNumber = it; selectedScheduleId = null },
        onPlaceSelected = { selectedScheduleId = it },
        onDetailClosed = { selectedScheduleId = null },
        onBackClick = onNavigateBack
    )
}

@Composable
fun FullCourseMapScreen(
    course: TravelCoursePresentationModel,
    selectedDayNumber: Int,
    selectedScheduleId: String?,
    onDaySelected: (Int) -> Unit,
    onPlaceSelected: (String?) -> Unit,
    onDetailClosed: () -> Unit,
    onBackClick: () -> Unit
) {
    val currentDayPlan = course.dayPlans.find { it.rawDayNumber == selectedDayNumber }
    val allPlaces = currentDayPlan?.schedules ?: emptyList()
    val selectedPlace = allPlaces.find { it.scheduleId == selectedScheduleId }
    val selectedIndex = allPlaces.indexOfFirst { it.scheduleId == selectedScheduleId }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(com.braveberry.tourdataproject.R.drawable.arrow_circle_left),
                            contentDescription = "뒤로가기",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column {
                        Text(course.courseName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(course.datePeriod, fontSize = 13.sp, color = Color.Gray)
                    }
                }
                HorizontalDivider()
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            KakaoMapSection(
                focusedSchedules = allPlaces.map { it.toScreen() },
                stay = currentDayPlan?.stay?.takeIf { it.scheduleId.isNotBlank() }?.toScreen()
            )

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                shadowElevation = 12.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    DayTabRow(
                        dayPlans = course.dayPlans,
                        selectedDayNumber = selectedDayNumber,
                        onDaySelected = onDaySelected
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedPlace == null) {
                        PlaceListSection(places = allPlaces, onPlaceClicked = onPlaceSelected)
                    } else {
                        PlaceDetailSection(
                            place = selectedPlace,
                            dayLabel = currentDayPlan?.dayLabel ?: "",
                            hasPrev = selectedIndex > 0,
                            hasNext = selectedIndex < allPlaces.lastIndex,
                            onPrev = { onPlaceSelected(allPlaces[selectedIndex - 1].scheduleId) },
                            onNext = { onPlaceSelected(allPlaces[selectedIndex + 1].scheduleId) },
                            onClose = onDetailClosed
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun DayTabRow(
    dayPlans: List<DayPlanPresentationModel>,
    selectedDayNumber: Int,
    onDaySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dayPlans.forEach { dayPlan ->
            val isSelected = dayPlan.rawDayNumber == selectedDayNumber
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) PrimaryTeal.copy(alpha = 0.15f) else Color(0xFFF2F2F2),
                modifier = Modifier.clickable { onDaySelected(dayPlan.rawDayNumber) }
            ) {
                Text(
                    text = dayPlan.dayLabel,
                    color = if (isSelected) PrimaryTeal else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
@Composable
private fun PlaceListSection(
    places: List<ScheduleItemPresentationModel>,
    onPlaceClicked: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        places.forEachIndexed { index, place ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaceClicked(place.scheduleId) }
                    .padding(vertical = 8.dp)
            ) {
                Surface(shape = CircleShape, color = PrimaryTeal, modifier = Modifier.size(24.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(place.scheduleName, fontSize = 15.sp)
            }
        }
    }
}
@Composable
private fun PlaceDetailSection(
    place: ScheduleItemPresentationModel,
    dayLabel: String,
    hasPrev: Boolean,
    hasNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, enabled = hasPrev) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward_ios),
                    contentDescription = "이전",
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = 180f } // 다음 버튼과 같은 리소스를 뒤집어 이전 버튼으로 사용
                )
            }
            Text(
                text = place.scheduleName,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onNext, enabled = hasNext) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward_ios),
                    contentDescription = "다음",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 100.dp)
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("사진", color = Color.Gray, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccessibilityRow(icon = R.drawable.elevator_icon, label = "엘리베이터")
                AccessibilityRow(icon = R.drawable.wheel_chair, label = "입구 경사로")
                AccessibilityRow(icon = R.drawable.wc, label = "장애인화장실 (남/여)")
                AccessibilityRow(icon = R.drawable.parking, label = "장애인 주차시설")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AccessibilityRow(icon: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = PrimaryTeal,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, color = Color.DarkGray)
    }
}

@Preview(showBackground = true)
@Composable
fun FullCourseMapScreenPreview() {
    val mockAccessibility = com.tourdataproject.presentation.model.plan.AccessibilityInfoPresentationModel()

    val mockSchedules = listOf(
        ScheduleItemPresentationModel(
            scheduleId = "1",
            order = 1,
            scheduleName = "가덕휴게소",
            latitude = 35.024,
            longitude = 128.825,
            accessibilityInfo = mockAccessibility
        ),
        ScheduleItemPresentationModel(
            scheduleId = "2",
            order = 2,
            scheduleName = "매미성",
            latitude = 34.975,
            longitude = 128.718,
            accessibilityInfo = mockAccessibility
        ),
        ScheduleItemPresentationModel(
            scheduleId = "3",
            order = 3,
            scheduleName = "학동흑진주몽돌해변",
            latitude = 34.761,
            longitude = 128.659,
            accessibilityInfo = mockAccessibility
        )
    )

    val mockStay = ScheduleItemPresentationModel(
        scheduleId = "stay1",
        order = 0,
        scheduleName = "거제 YAHO HOTEL",
        latitude = 34.880,
        longitude = 128.621
    )

    val mockDayPlans = listOf(
        DayPlanPresentationModel(
            dayLabel = "Day 1",
            dateLabel = "8/30",
            rawDayNumber = 1,
            rawDate = 0L,
            schedules = mockSchedules,
            stay = mockStay
        ),
        DayPlanPresentationModel(
            dayLabel = "Day 2",
            dateLabel = "8/31",
            rawDayNumber = 2,
            rawDate = 0L,
            schedules = emptyList(),
            stay = mockStay
        )
    )

    val mockCourse = TravelCoursePresentationModel(
        courseId = "course1",
        courseName = "거제 여행",
        datePeriod = "2026.08.30 ~ 2026.08.31",
        dayPlans = mockDayPlans
    )

    FullCourseMapScreen(
        course = mockCourse,
        selectedDayNumber = 1,
        selectedScheduleId = null,
        onDaySelected = {},
        onPlaceSelected = {},
        onDetailClosed = {},
        onBackClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun FullCourseMapScreenDetailPreview() {
    val mockPlace = ScheduleItemPresentationModel(
        scheduleId = "1",
        order = 1,
        scheduleName = "가덕휴게소",
        latitude = 35.024,
        longitude = 128.825
    )

    val mockDayPlans = listOf(
        DayPlanPresentationModel(
            dayLabel = "Day 1",
            dateLabel = "8/30",
            rawDayNumber = 1,
            rawDate = 0L,
            schedules = listOf(mockPlace)
        )
    )

    val mockCourse = TravelCoursePresentationModel(
        courseId = "course1",
        courseName = "거제 여행",
        datePeriod = "2026.08.30 ~ 2026.08.31",
        dayPlans = mockDayPlans
    )

    FullCourseMapScreen(
        course = mockCourse,
        selectedDayNumber = 1,
        selectedScheduleId = "1",
        onDaySelected = {},
        onPlaceSelected = {},
        onDetailClosed = {},
        onBackClick = {}
    )
}


