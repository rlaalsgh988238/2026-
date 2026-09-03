package com.braveberry.tourdataproject.screen.plan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.braveberry.tourdataproject.R
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.route.RouteLineStylesSet
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.ScheduleEditViewModel
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEffect
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEvent
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditState

@Composable
fun ScheduleEditRoute(
    sharedViewModel: PlanSharedViewModel,
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
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
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

    LaunchedEffect(focusedSchedules, mapInstance, context) {
        val map = mapInstance ?: return@LaunchedEffect
        if (focusedSchedules.isEmpty()) return@LaunchedEffect

        val labelManager = map.labelManager
        val routeLineManager = map.routeLineManager

        labelManager?.layer?.removeAll()
        routeLineManager?.layer?.removeAll()

        val points = mutableListOf<LatLng>()

        focusedSchedules.forEachIndexed { index, schedule ->
            val latLng = LatLng.from(schedule.latitude, schedule.longitude)
            points.add(latLng)

            val isAccommodation = index == focusedSchedules.lastIndex

            val bitmap = createCustomMarkerBitmap(context, "${index + 1}", isAccommodation)

            val style = LabelStyles.from(
                LabelStyle.from(bitmap).setAnchorPoint(0.5f, 0.5f)
            )

            val options = LabelOptions.from(latLng).setStyles(style)
            labelManager?.layer?.addLabel(options)
        }

        if (points.size > 1) {
            points.add(points.first())

            val routeStyle = RouteLineStyle.from(4f, android.graphics.Color.parseColor("#888888"))
            val routeStylesSet = RouteLineStylesSet.from("route", RouteLineStyles.from(routeStyle))
            val segment = RouteLineSegment.from(points).setStyles(routeStylesSet.getStyles(0))
            val options = RouteLineOptions.from(segment)
            routeLineManager?.layer?.addRouteLine(options)
        }

        val cameraUpdate = CameraUpdateFactory.newCenterPosition(points.first(), 10)
        map.moveCamera(cameraUpdate)
    }
}

private fun createCustomMarkerBitmap(context: Context, text: String, isAccommodation: Boolean): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (24 * density).toInt() // 마커 크기
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 1. 배경 원 그리기
    paint.color = if (isAccommodation) android.graphics.Color.parseColor("#FFC107") // 노란색
    else android.graphics.Color.parseColor("#14B8A6") // 민트색
    paint.style = Paint.Style.FILL
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    // 2. 내부 콘텐츠 그리기
    if (isAccommodation) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.home)
        if (drawable != null) {
            // 아이콘 크기 원 크기에 맞게
            val iconSize = (14 * density).toInt()
            val left = (size - iconSize) / 2
            val top = (size - iconSize) / 2

            drawable.setBounds(left, top, left + iconSize, top + iconSize)
            drawable.setTint(android.graphics.Color.WHITE) // 아이콘 색상을 흰색으로 변경
            drawable.draw(canvas)
        }
    } else {
        paint.color = android.graphics.Color.WHITE
        // 숫자 그리기
        paint.textSize = 13 * density
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        // 텍스트 수직 중앙 정렬 보정
        val y = (size / 2f) + (textBounds.height() / 2f) - (0.5f * density)

        canvas.drawText(text, size / 2f, y, paint)
    }

    return bitmap
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

        itemsIndexed(items = state.schedules, key = { _, schedule -> schedule.scheduleId }) { index, schedule ->
            val isAccommodation = index == state.schedules.lastIndex
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
                        .padding(horizontal = 16.dp, vertical = if (isAccommodation) 8.dp else 14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        if (isAccommodation) {
                            Text(
                                text = "숙소",
                                fontSize = 15.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Text(
                            text = schedule.scheduleName,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    }
                }

                if (!isAccommodation) {
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
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y

                                        val currentIndex = currentSchedules.indexOfFirst { it.scheduleId == schedule.scheduleId }
                                        val currentItemInfo = listState.layoutInfo.visibleItemsInfo.find { it.key == schedule.scheduleId }

                                        if (currentItemInfo != null && currentIndex != -1) {
                                            if (currentItemInfo.index != currentIndex + 1) {
                                                return@detectDragGestures
                                            }

                                            val visualCenterY = currentItemInfo.offset + (currentItemInfo.size / 2) + dragOffsetY

                                            val targetItemInfo = listState.layoutInfo.visibleItemsInfo.find {
                                                it.key != schedule.scheduleId &&
                                                        it.key != "header" &&
                                                        visualCenterY >= it.offset && visualCenterY <= (it.offset + it.size)
                                            }

                                            if (targetItemInfo != null) {
                                                val targetIndex = currentSchedules.indexOfFirst { it.scheduleId == targetItemInfo.key }

                                                if (targetIndex != -1 && currentIndex != targetIndex && targetIndex < currentSchedules.lastIndex) {
                                                    val direction = if (targetIndex > currentIndex) 1 else -1

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
                                    },
                                    onDragCancel = {
                                        draggedId = null
                                        dragOffsetY = 0f
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleEditScreenPreview() {
    val dummyState = ScheduleEditState(
        dayNumber = 1,
        dateLabel = "8/30 (일)",
        schedules = listOf(
            ScheduleItemUiModel(scheduleId = "1", order = 1, scheduleName = "가덕휴게소", latitude = 35.024, longitude = 128.825),
            ScheduleItemUiModel(scheduleId = "2", order = 2, scheduleName = "매미성", latitude = 34.975, longitude = 128.718),
            ScheduleItemUiModel(scheduleId = "3", order = 3, scheduleName = "바람의 언덕", latitude = 34.761, longitude = 128.659),
            ScheduleItemUiModel(scheduleId = "4", order = 4, scheduleName = "거제 파노라마 케이블카", latitude = 34.801, longitude = 128.623),
            ScheduleItemUiModel(scheduleId = "5", order = 5, scheduleName = "거제 YAHO HOTEL", latitude = 34.880, longitude = 128.621)
        )
    )

    ScheduleEditScreen(
        state = dummyState,
        onEvent = {}
    )
}
