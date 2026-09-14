package com.braveberry.tourdataproject.screen.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.R
import com.tourdataproject.presentation.viewmodel.course.courseList.CourseListViewModel
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListEffect
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListItemState
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListUiState

// 색상 정의
val MintCardBg = Color(0xFFE4F2F1)
val MintCardBorder = Color(0xFF26A69A)
val PeachCardBg = Color(0xFFFCEBE9)
val PeachCardBorder = Color(0xFFE57373)
val YellowFabBg = Color(0xFFFFD54F)

@Composable
fun ListRoute(
    listViewModel: CourseListViewModel = hiltViewModel(),
    onNavigateToCreateNewCourse: () -> Unit,
    onNavigateToCourseDetail: (String) -> Unit = {},
    onNavigateToNearbyToilet: () -> Unit,
    onShowToast: (String) -> Unit = {}
) {
    val uiState by listViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current


    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            listViewModel.onRestroomGuideClicked()
        } else {
            onShowToast("근처 긴급 화장실을 찾으려면 위치 권한이 필요합니다.")
        }
    }

    LaunchedEffect(Unit) {
        listViewModel.loadCourses()
    }

    LaunchedEffect(listViewModel.effect) {
        listViewModel.effect.collect { effect ->
            when (effect) {
                is CourseListEffect.NavigateToCreatePlan -> onNavigateToCreateNewCourse()
                is CourseListEffect.NavigateToRestroomGuide -> { onNavigateToNearbyToilet()}
                is CourseListEffect.NavigateToCourseDetail -> onNavigateToCourseDetail(effect.courseId)
                is CourseListEffect.ShowToast -> onShowToast(effect.message)
            }
        }
    }

    CourseListScreen(
        state = uiState,
        onAddClick = listViewModel::onCreatePlanClicked,
        onCourseClick = { clickedCourseId -> listViewModel.onCourseClicked(clickedCourseId) },
        onRestroomGuideClick = {
             val hasFineLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED


            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                // 이미 권한이 있다면 바로 기능 실행
                listViewModel.onRestroomGuideClicked()
            } else {
                // 권한이 없다면 시스템 팝업을 띄워 요청
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    )
}





@Composable
fun CourseListScreen(
    state: CourseListUiState,
    onAddClick: () -> Unit,
    onCourseClick: (String) -> Unit,
    onRestroomGuideClick: () -> Unit
) {
    Scaffold(containerColor = Color.White) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // 로고 영역
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. 앱 아이콘 로고
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(110.dp) // 아이콘도 살짝 키움
                    )

                    // 2. 로고 텍스트 (화면의 85%까지 차지하도록 확대)
                    Image(
                        painter = painterResource(id = R.drawable.logo_string),
                        contentDescription = "변수없길",
                        // FillWidth를 주어야 이미지 안의 글자가 실제 영역만큼 커집니다.
                        contentScale = androidx.compose.ui.layout.ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth(0.85f) // 0.6에서 0.85로 대폭 상향
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // 필터 탭
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(text = "전체", selected = true)
                    FilterChip(text = "예정된 여행", selected = false)
                    FilterChip(text = "다녀온 여행", selected = false)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 리스트 영역
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.courses) { itemState ->
                        CourseCardItem(itemState = itemState, onClick = { onCourseClick(itemState.courseId) })
                    }
                    item { Spacer(modifier = Modifier.height(120.dp)) } // 하단 버튼 공간 확보
                }
            }

            // 하단 고정 버튼 (이미지 1 스타일)
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 32.dp, end = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RestroomGuideButton(onClick = onRestroomGuideClick)

                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    containerColor = Color(0xFFF7CD18),
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(30.dp),
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("일정 만들기", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun FilterChip(text: String, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFFE0F2F1) else Color.White,
        border = BorderStroke(1.dp, if (selected) Color(0xFF26A69A) else Color.LightGray),
        modifier = Modifier.clickable { }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = if (selected) Color(0xFF26A69A) else Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestroomGuideButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        color = PeachCardBg,
        border = BorderStroke(1.dp, PeachCardBorder),
        modifier = Modifier.height(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 이미지 속 느낌표(경고) 아이콘
            Icon(
                painter = painterResource(id = R.drawable.accessible), // 적절한 아이콘 리소스 확인 필요
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("긴급 화장실", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        }
    }
}

@Composable
fun CourseCardItem(itemState: CourseListItemState, onClick: () -> Unit) {
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(MintCardBorder, RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Text(text = itemState.dDayText, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
                IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "더보기", tint = Color.DarkGray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = itemState.courseName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = itemState.datePeriod, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun CourseListScreenPreview() {
    CourseListScreen(
        state = CourseListUiState(courses = listOf(CourseListItemState("1", "거제 여행", "2026.08.30 ~ 2026.08.31", "D-6"))),
        onAddClick = {}, onCourseClick = {}, onRestroomGuideClick = {}
    )
}
