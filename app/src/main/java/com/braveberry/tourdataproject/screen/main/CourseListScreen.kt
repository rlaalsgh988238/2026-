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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.R
import com.tourdataproject.presentation.viewmodel.course.courseList.CourseListViewModel
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListEffect
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListIntent // 🌟 Intent 임포트 추가
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListItemState
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.CourseListUiState
import com.tourdataproject.presentation.viewmodel.course.courseList.uiState.TravelFilter

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
    onNavigateToEditCourseName: (String, String) -> Unit,
    onShowToast: (String) -> Unit = {}
) {
    val uiState by listViewModel.state.collectAsStateWithLifecycle()
    val selectedFilter by listViewModel.selectedFilter.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            // 🌟 MVI 적용: 함수 직접 호출 대신 Intent 전달
            listViewModel.onIntent(CourseListIntent.OnRestroomGuideClicked)
        } else {
            onShowToast("근처 긴급 화장실을 찾으려면 위치 권한이 필요합니다.")
        }
    }

    LaunchedEffect(Unit) {
        listViewModel.onIntent(CourseListIntent.OnLoadCourses)
    }

    LaunchedEffect(listViewModel.effect) {
        listViewModel.effect.collect { effect ->
            when (effect) {
                is CourseListEffect.NavigateToCreatePlan -> onNavigateToCreateNewCourse()
                is CourseListEffect.NavigateToRestroomGuide -> onNavigateToNearbyToilet()
                is CourseListEffect.NavigateToCourseDetail -> onNavigateToCourseDetail(effect.courseId)
                is CourseListEffect.ShowToast -> onShowToast(effect.message)
                is CourseListEffect.NavigateToEditCourseName ->
                    onNavigateToEditCourseName(effect.courseId, effect.initialName)
            }
        }
    }

    CourseListScreen(
        state = uiState,
        selectedFilter = selectedFilter,
        onIntent = listViewModel::onIntent,
        onRestroomPermissionCheck = {
            val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                listViewModel.onIntent(CourseListIntent.OnRestroomGuideClicked) // 🌟 MVI 적용
            } else {
                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    )
}

@Composable
fun CourseListScreen(
    state: CourseListUiState,
    selectedFilter: TravelFilter,
    onIntent: (CourseListIntent) -> Unit,
    onRestroomPermissionCheck: () -> Unit
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
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(110.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo_string),
                        contentDescription = "변수없길",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // 필터 탭
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TravelFilter.values().forEach { filter ->
                        FilterChip(
                            text = filter.text,
                            selected = selectedFilter == filter,
                            // 🌟 필터 클릭 인텐트 발생
                            onClick = { onIntent(CourseListIntent.OnFilterChanged(filter)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 리스트 영역
                if (state.isLoading) {
                    Box(Modifier.weight(1f)) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center), color = MintCardBorder)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(state.courses, key = { it.courseId }) { itemState ->
                            CourseCardItem(
                                itemState = itemState,
                                // 🌟 상세 이동 인텐트 발생
                                onClick = { onIntent(CourseListIntent.OnCourseClicked(itemState.courseId)) },
                                onEditNameClick = {
                                    onIntent(CourseListIntent.OnEditNameClicked(itemState.courseId, itemState.courseName))
                                },
                                // 🌟 삭제 인텐트 발생 연결
                                onDeleteClick = { onIntent(CourseListIntent.OnDeleteCourseClicked(itemState.courseId)) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(120.dp)) }
                    }
                }
            }

            // 하단 고정 버튼
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 32.dp, end = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RestroomGuideButton(onClick = onRestroomPermissionCheck)

                ExtendedFloatingActionButton(
                    // 🌟 생성 화면 이동 인텐트 발생
                    onClick = { onIntent(CourseListIntent.OnCreatePlanClicked) },
                    containerColor = YellowFabBg,
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
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFFE0F2F1) else Color.White,
        border = BorderStroke(1.dp, if (selected) Color(0xFF26A69A) else Color.LightGray),
        modifier = Modifier.clickable { onClick() }
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
    // 기존 코드 동일 유지...
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
            Icon(
                painter = painterResource(id = R.drawable.accessible),
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
fun CourseCardItem(
    itemState: CourseListItemState,
    onClick: () -> Unit,
    onEditNameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showActionDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) } // 🌟 삭제 확인 팝업 상태 추가

    if (showActionDialog) {
        CourseActionDialog(
            onDismiss = { showActionDialog = false },
            onEditNameClick = {
                showActionDialog = false
                onEditNameClick()
            },
            onEditDateClick = {
                showActionDialog = false
                // TODO: 날짜 변경 인텐트 연결
            },
            onDeleteClick = {
                showActionDialog = false
                showDeleteConfirmDialog = true
            }
        )
    }

    if (showDeleteConfirmDialog) {
        CourseDeleteConfirmDialog(
            courseName = itemState.courseName,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                showDeleteConfirmDialog = false
                onDeleteClick() // 실제 삭제 실행
            }
        )
    }

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

                IconButton(onClick = { showActionDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "더보기", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = itemState.courseName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = itemState.datePeriod, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CourseDeleteConfirmDialog(
    courseName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp), // 둥근 모서리 비율 반영
            color = Color.White,
            modifier = Modifier
                .width(412.dp) // 시안 가로 비율
                .height(205.dp) // 시안 세로 비율
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 타이틀 텍스트
                Text(
                    text = "$courseName 플랜을 삭제할까요?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0E0E0),
                        border = BorderStroke(2.dp, Color(0xFFB3B3B3)),
                        modifier = Modifier
                            .weight(1f)
                            .height(67.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "취소",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MintCardBorder,
                        modifier = Modifier
                            .weight(1f)
                            .height(67.dp)
                            .clickable { onConfirm() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "삭제",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseActionDialog(
    onDismiss: () -> Unit,
    onEditNameClick: () -> Unit,
    onEditDateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            // 🌟 테두리 두께 2.dp, 색상 #B3B3B3 적용
            border = BorderStroke(2.dp, Color(0xFFB3B3B3)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Text(
                    text = "여행 이름 변경",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditNameClick() }
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                Text(
                    text = "여행 도시/ 날짜변경",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditDateClick() }
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )

                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                Text(
                    text = "일정 삭제",
                    fontSize = 16.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDeleteClick() }
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun CourseListScreenPreview() {
    CourseListScreen(
        state = CourseListUiState(
            isLoading = false,
            courses = listOf(
                CourseListItemState("1", "거제 여행", "2026.08.30 ~ 2026.08.31", "D-6"),
                CourseListItemState("2", "제주도 여행", "2026.01.01 ~ 2026.01.05", "D+200")
            )
        ),
        selectedFilter = TravelFilter.ALL,
        onIntent = {},
        onRestroomPermissionCheck = {}
    )
}