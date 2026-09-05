package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tourdataproject.presentation.viewmodel.course.courseList.CourseListViewModel
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListEffect
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListItemState
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListUiState
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel

// ================= 색상 정의 (이미지 기반 파스텔 톤) =================
val MintCardBg = Color(0xFFE4F2F1)
val MintCardBorder = Color(0xFF26A69A)
val PeachCardBg = Color(0xFFFCEBE9)
val PeachCardBorder = Color(0xFFE57373)
val YellowFabBg = Color(0xFFFFD54F)


@Composable
fun ListRoute(
    sharedViewModel: PlanSharedViewModel,
    listViewModel: CourseListViewModel = hiltViewModel(),
    onNavigateToCreateNewCourse: () -> Unit,
    onNavigateToCourseDetail: () -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {

    val uiState by listViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        listViewModel.loadCourses()
    }

    LaunchedEffect(listViewModel.effect) {
        listViewModel.effect.collect { effect ->
            when (effect) {
                is CourseListEffect.NavigateToCreatePlan -> onNavigateToCreateNewCourse()
                is CourseListEffect.NavigateToRestroomGuide -> { /* TODO */ }
                is CourseListEffect.NavigateToCourseDetail -> {
                    sharedViewModel.onIntent(PlanSharedIntent.OnLoadCourseById(effect.courseId))
                    onNavigateToCourseDetail()
                }
                is CourseListEffect.ShowToast -> onShowToast(effect.message)
            }
        }
    }

    if (uiState.isError) {
        LaunchedEffect(uiState.errorMessage) {
            onShowToast(uiState.errorMessage ?: "오류가 발생했습니다.")
        }
    } else {
        CourseListScreen(
            state = uiState,
            onAddClick = listViewModel::onCreatePlanClicked,
            onCourseClick = { clickedCourseId -> listViewModel.onCourseClicked(clickedCourseId) },
            onRestroomGuideClick = listViewModel::onRestroomGuideClicked
        )
    }
}

@Composable
fun CourseListScreen(
    state: CourseListUiState,
    onAddClick: () -> Unit,
    onCourseClick: (String) -> Unit,
    onRestroomGuideClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = YellowFabBg,
                contentColor = Color.Black,
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "플랜 만들기")
                Spacer(Modifier.width(4.dp))
                Text("플랜만들기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            //TODO: 로고 자리
            Box(modifier = Modifier.size(120.dp).background(Color.LightGray, RoundedCornerShape(50)))

            Spacer(modifier = Modifier.height(40.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                items(state.courses) { itemState ->
                    CourseCardItem(
                        itemState = itemState,
                        onClick = { onCourseClick(itemState.courseId) }
                    )
                }

                item {
                    RestroomGuideButton(onClick = onRestroomGuideClick)
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun CourseCardItem(
    itemState: CourseListItemState,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MintCardBg)
            .border(width = 1.dp, color = MintCardBorder, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MintCardBorder, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = itemState.dDayText,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = { /* TODO: 옵션 메뉴 열기 */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "더보기",
                        tint = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = itemState.courseName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = itemState.datePeriod,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RestroomGuideButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PeachCardBg)
            .border(width = 1.dp, color = PeachCardBorder, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = "주의 마크",
            tint = Color.Red,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "근처 화장실 안내",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

// ----------------------------------------------------
// 미리보기 (Preview)
// ----------------------------------------------------
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun CourseListScreenPreview() {
    CourseListScreen(
        state = CourseListUiState(
            isLoading = false,
            courses = listOf(
                CourseListItemState(
                    courseId = "1",
                    courseName = "거제 여행",
                    datePeriod = "2026.08.30 ~ 2026.08.31",
                    dDayText = "D-6"
                )
            )
        ),
        onAddClick = {},
        onCourseClick = {},
        onRestroomGuideClick = {}
    )
}
