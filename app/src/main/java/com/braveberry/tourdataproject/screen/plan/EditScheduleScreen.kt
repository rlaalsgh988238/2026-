package com.braveberry.tourdataproject.screen.plan

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.ScheduleEditViewModel
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEffect
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEvent
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditState

@Composable
fun ScheduleEditRoute(
    viewModel: ScheduleEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScheduleEditEffect.NavigateBack -> onNavigateBack()
                is ScheduleEditEffect.ShowToast -> onShowToast(effect.message)
            }
        }
    }

    ScheduleEditScreen(
        state = state,
        onEvent = viewModel::setEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    state: ScheduleEditState,
    onEvent: (ScheduleEditEvent) -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("일정 편집", fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ScheduleEditEvent.OnBackClicked) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(onClick = { onEvent(ScheduleEditEvent.OnSaveClicked) }) {
                        Text("저장", color = PrimaryTeal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                KakaoMapSection(focusedSchedules = state.schedules)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .background(Color(0xFFFDFDFD))
            ) {
                ScheduleListSection(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
fun KakaoMapSection(focusedSchedules: List<ScheduleItemUiModel>) {
    var mapInstance by remember { mutableStateOf<KakaoMap?>(null) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() { Log.d("KakaoMap", "지도 소멸됨") }
                        override fun onMapError(error: Exception?) { Log.e("KakaoMap", "지도 에러: ${error?.message}") }
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            mapInstance = kakaoMap
                        }
                    }
                )
            }
        }
    )

    LaunchedEffect(focusedSchedules, mapInstance) {
        val map = mapInstance ?: return@LaunchedEffect
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleListSection(
    state: ScheduleEditState,
    onEvent: (ScheduleEditEvent) -> Unit
) {
    val listState = rememberLazyListState()

    var draggedId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    // 🌟 핵심 수정: pointerInput 내부에서 항상 최신 상태의 리스트를 참조하도록 감싸줍니다.
    val currentSchedules by rememberUpdatedState(state.schedules)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item(key = "header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Surface(
                    color = PrimaryTeal.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = state.dayLabel,
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.dateLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }

        items(items = state.schedules, key = { it.scheduleId }) { schedule ->
            val isDragging = draggedId == schedule.scheduleId
            val translation = if (isDragging) dragOffsetY else 0f

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = translation }
                    .let { if (isDragging) it else it.animateItem() }
            ) {
                IconButton(
                    onClick = { onEvent(ScheduleEditEvent.OnScheduleDeleted(schedule.scheduleId)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier.size(22.dp).background(Color.Red, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.width(10.dp).height(2.dp).background(Color.White))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, PrimaryTeal, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDragging) Color.LightGray.copy(alpha = 0.8f) else Color.White)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = schedule.scheduleName,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "순서 변경",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                        .pointerInput(schedule.scheduleId) {
                            detectDragGestures(
                                onDragStart = {
                                    draggedId = schedule.scheduleId
                                    dragOffsetY = 0f
                                    Log.d("DragAndDrop", "드래그 시작: ${schedule.scheduleId}")
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y

                                    // 🌟 수정: state.schedules 대신 항상 최신인 currentSchedules를 참조합니다.
                                    val currentIndex = currentSchedules.indexOfFirst { it.scheduleId == schedule.scheduleId }
                                    val currentItemInfo = listState.layoutInfo.visibleItemsInfo.find { it.key == schedule.scheduleId }

                                    if (currentItemInfo != null && currentIndex != -1) {
                                        if (currentItemInfo.index != currentIndex + 1) {
                                            Log.w("DragAndDrop", "레이아웃 갱신 대기 중 (상태 인덱스: $currentIndex, 레이아웃 인덱스: ${currentItemInfo.index})")
                                            return@detectDragGestures
                                        }

                                        val visualCenterY = currentItemInfo.offset + (currentItemInfo.size / 2) + dragOffsetY

                                        val targetItemInfo = listState.layoutInfo.visibleItemsInfo.find {
                                            it.key != schedule.scheduleId &&
                                                    it.key != "header" &&
                                                    visualCenterY >= it.offset && visualCenterY <= (it.offset + it.size)
                                        }

                                        if (targetItemInfo != null) {
                                            // 🌟 수정: targetIndex 계산에도 currentSchedules 적용
                                            val targetIndex = currentSchedules.indexOfFirst { it.scheduleId == targetItemInfo.key }

                                            if (targetIndex != -1 && currentIndex != targetIndex) {
                                                val direction = if (targetIndex > currentIndex) 1 else -1

                                                // 🌟 수정: itemsToShift 계산에도 currentSchedules 적용
                                                val itemsToShift = currentSchedules.slice(
                                                    if (direction == 1) (currentIndex + 1)..targetIndex
                                                    else targetIndex until currentIndex
                                                )

                                                var totalShiftPx = 0
                                                itemsToShift.forEach { shiftItem ->
                                                    val info = listState.layoutInfo.visibleItemsInfo.find { it.key == shiftItem.scheduleId }
                                                    totalShiftPx += info?.size ?: currentItemInfo.size
                                                }

                                                if (totalShiftPx > 0) {
                                                    Log.d("DragAndDrop", "위치 변경: $currentIndex -> $targetIndex (이동 픽셀: $totalShiftPx)")
                                                    onEvent(ScheduleEditEvent.OnScheduleMoved(currentIndex, targetIndex))
                                                    dragOffsetY -= (totalShiftPx * direction)
                                                }
                                            }
                                        }
                                    }
                                },
                                onDragEnd = {
                                    draggedId = null
                                    dragOffsetY = 0f
                                    onEvent(ScheduleEditEvent.OnScheduleMoveFinished)
                                    Log.d("DragAndDrop", "드래그 종료")
                                },
                                onDragCancel = {
                                    draggedId = null
                                    dragOffsetY = 0f
                                    Log.d("DragAndDrop", "드래그 취소됨")
                                }
                            )
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleEditScreenPreview() {
    val dummyState = ScheduleEditState(
        dayNumber = 1,
        dayLabel = "Day 1",
        dateLabel = "8/30 (일)",
        schedules = listOf(
            ScheduleItemUiModel(scheduleId = "1", order = 1, scheduleName = "가덕휴게소"),
            ScheduleItemUiModel(scheduleId = "2", order = 2, scheduleName = "매미성"),
            ScheduleItemUiModel(scheduleId = "3", order = 3, scheduleName = "거제 YAHO HOTEL")
        )
    )

    ScheduleEditScreen(
        state = dummyState,
        onEvent = {}
    )
}
