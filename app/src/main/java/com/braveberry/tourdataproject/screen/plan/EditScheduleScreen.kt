package com.braveberry.tourdataproject.screen.plan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.R
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.route.RouteLineStylesSet
import com.tourdataproject.presentation.model.plan.ScheduleItemPresentationModel
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedState
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.ScheduleEditViewModel
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditEffect
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditIntent
import com.tourdataproject.presentation.viewmodel.plan.scheduleEdit.uiState.ScheduleEditState

@Composable
fun ScheduleEditRoute(
    sharedViewModel: PlanSharedViewModel,
    viewModel: ScheduleEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onShowToast: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()

    LaunchedEffect(sharedState.course.dayPlans) {
        val dayPlan = sharedState.course.dayPlans.find { it.rawDayNumber == viewModel.dayNum }
        if (dayPlan != null) {
            val stay = dayPlan.stay.takeIf { it.scheduleId.isNotBlank() }
            viewModel.onIntent(ScheduleEditIntent.OnInit(dayPlan.dateLabel, dayPlan.schedules, stay))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ScheduleEditEffect.NavigateBack -> onNavigateBack()
                is ScheduleEditEffect.ShowToast -> onShowToast(effect.message)
                is ScheduleEditEffect.SaveToShared -> {
                    sharedViewModel.onIntent(PlanSharedIntent.OnReorderSchedules(effect.dayNumber, effect.schedules))
                }
                is ScheduleEditEffect.DeleteStayFromShared -> {
                    sharedViewModel.onIntent(PlanSharedIntent.OnDeleteStay(effect.scheduleId))
                }
            }
        }
    }

    ScheduleEditScreen(
        state = state,
        sharedState = sharedState,
        onIntent = viewModel::onIntent,
        onSharedIntent = sharedViewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    state: ScheduleEditState,
    sharedState: PlanSharedState,
    onIntent: (ScheduleEditIntent) -> Unit,
    onSharedIntent: (PlanSharedIntent) -> Unit
) {
    val schedules = state.schedules.map { it.toScreen() }
    val stay = state.stay?.toScreen()
    val listState = rememberLazyListState()
    val dragDropState = rememberScheduleDragDropState(
        listState = listState,
        onMoveRequest = { from, to -> onIntent(ScheduleEditIntent.OnScheduleMoved(from, to)) }
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "일정 편집",
                            fontSize = 20.sp, // 20sp로 변경
                            fontWeight = FontWeight.Bold, // Bold로 변경
                            color = Color.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { onIntent(ScheduleEditIntent.OnBackClicked) },
                            modifier = Modifier.size(40.dp) // 40dp로 고정
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_circle_left),
                                contentDescription = "뒤로가기",
                                modifier = Modifier.fillMaxSize(), // 꽉 채움
                                tint = Color.Unspecified
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { onIntent(ScheduleEditIntent.OnSaveClicked) }) {
                            Text(
                                text = "저장",
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp) // 좌측 여백 확보
                )
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            }
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
                // ScheduleEditScreen에서는 카메라 포커스 불필요 → null 고정
                KakaoMapSection(
                    focusedSchedules = schedules,
                    stay = stay,
                    draggedId = dragDropState.draggedId,
                    cameraFocusLatLng = null,
                    onCameraFocusConsumed = {}
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .background(Color(0xFFFDFDFD))
            ) {
                ScheduleListSection(
                    dayLabel = state.dayLabel,
                    dateLabel = state.dateLabel,
                    schedules = schedules,
                    stay = stay,
                    dragDropState = dragDropState,
                    onEvent = onIntent
                )
            }
        }
    }
}

@Composable
fun KakaoMapSection(
    focusedSchedules: List<ScheduleItemScreenModel>,
    stay: ScheduleItemScreenModel? = null,
    draggedId: String? = null,
    cameraFocusLatLng: Pair<Double, Double>? = null,
    onCameraFocusConsumed: () -> Unit = {}
) {
    var mapInstance by remember { mutableStateOf<KakaoMap?>(null) }
    val context = LocalContext.current
    val bitmapCache = remember { mutableMapOf<String, Bitmap>() }
    var isInitialFocusDone by remember { mutableStateOf(false) }

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

    // 마커 및 라인 업데이트
    LaunchedEffect(focusedSchedules, stay, mapInstance, context) {
        val map = mapInstance ?: return@LaunchedEffect
        if (focusedSchedules.isEmpty() && stay == null) return@LaunchedEffect

        val labelManager = map.labelManager
        val routeLineManager = map.routeLineManager

        labelManager?.layer?.removeAll()
        routeLineManager?.layer?.removeAll()

        val points = mutableListOf<LatLng>()

        focusedSchedules.forEachIndexed { index, schedule ->
            val latLng = LatLng.from(schedule.latitude, schedule.longitude)
            points.add(latLng)

            val cacheKey = "${index + 1}"
            val bitmap = bitmapCache.getOrPut(cacheKey) {
                createCustomMarkerBitmap(context, "${index + 1}", isAccommodation = false)
            }

            val style = LabelStyles.from(LabelStyle.from(bitmap).setAnchorPoint(0.5f, 0.5f))
            val options = LabelOptions.from(latLng).setStyles(style)
            labelManager?.layer?.addLabel(options)
        }

        if (stay != null) {
            val stayLatLng = LatLng.from(stay.latitude, stay.longitude)
            points.add(stayLatLng)

            val bitmap = bitmapCache.getOrPut("accommodation") {
                createCustomMarkerBitmap(context, "", isAccommodation = true)
            }
            val style = LabelStyles.from(LabelStyle.from(bitmap).setAnchorPoint(0.5f, 0.5f))
            val options = LabelOptions.from(stayLatLng).setStyles(style)
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

        if (!isInitialFocusDone) {
            val firstPoint = focusedSchedules.firstOrNull()
                ?.let { LatLng.from(it.latitude, it.longitude) }
                ?: stay?.let { LatLng.from(it.latitude, it.longitude) }
            if (firstPoint != null) {
                map.moveCamera(CameraUpdateFactory.newCenterPosition(firstPoint, 10))
                isInitialFocusDone = true
            }
        }
    }

    // 외부에서 요청한 좌표로 카메라 이동
    LaunchedEffect(cameraFocusLatLng) {
        val map = mapInstance ?: return@LaunchedEffect
        cameraFocusLatLng ?: return@LaunchedEffect
        val (lat, lng) = cameraFocusLatLng
        val currentZoom = map.cameraPosition?.zoomLevel ?: 15
        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(LatLng.from(lat, lng), currentZoom)
        )
        onCameraFocusConsumed()
    }

    // 드래그 중 카메라 이동
    LaunchedEffect(draggedId, mapInstance) {
        val map = mapInstance ?: return@LaunchedEffect
        if (draggedId != null) {
            val targetSchedule = focusedSchedules.find { it.scheduleId == draggedId }
            if (targetSchedule != null) {
                val currentZoom = map.cameraPosition?.zoomLevel ?: 10
                map.moveCamera(
                    CameraUpdateFactory.newCenterPosition(
                        LatLng.from(targetSchedule.latitude, targetSchedule.longitude),
                        currentZoom
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleListSection(
    dayLabel: String,
    dateLabel: String,
    schedules: List<ScheduleItemScreenModel>,
    stay: ScheduleItemScreenModel?,
    dragDropState: ScheduleDragDropState,
    onEvent: (ScheduleEditIntent) -> Unit
) {
    val currentSchedules by rememberUpdatedState(schedules)

    LazyColumn(
        state = dragDropState.listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item(key = "header") {
            ScheduleListHeader(dayLabel = dayLabel, dateLabel = dateLabel)
        }

        itemsIndexed(items = schedules, key = { _, schedule -> schedule.scheduleId }) { _, schedule ->
            val isDragging = dragDropState.draggedId == schedule.scheduleId
            val translation = if (isDragging) dragDropState.dragOffsetY else 0f

            ScheduleListItem(
                schedule = schedule,
                isDragging = isDragging,
                translationY = translation,
                modifier = Modifier.animateItem(),
                onDelete = { onEvent(ScheduleEditIntent.OnScheduleDeleted(schedule.scheduleId)) },
                onDragStart = { dragDropState.onDragStart(schedule.scheduleId) },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragDropState.onDrag(dragAmount.y, currentSchedules)
                },
                onDragEnd = {
                    dragDropState.onDragInterrupted()
                    onEvent(ScheduleEditIntent.OnScheduleMoveFinished)
                },
                onDragCancel = { dragDropState.onDragInterrupted() }
            )
        }

        if (stay != null) {
            item(key = "stay") {
                StayListItem(
                    stay = stay,
                    onDelete = { onEvent(ScheduleEditIntent.OnStayDeleted) }
                )
            }
        }
    }
}

@Composable
private fun ScheduleListHeader(dayLabel: String, dateLabel: String) {
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
                text = dayLabel,
                color = PrimaryTeal,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dateLabel,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun ScheduleListItem(
    schedule: ScheduleItemScreenModel,
    isDragging: Boolean,
    translationY: Float,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (PointerInputChange, Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer { this.translationY = translationY }
            .then(if (isDragging) Modifier else modifier)
    ) {
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color.Red, CircleShape),
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
            Text(text = schedule.scheduleName, fontSize = 15.sp, color = Color.Black)
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
                        onDragStart = { onDragStart() },
                        onDrag = { change, dragAmount -> onDrag(change, dragAmount) },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragCancel() }
                    )
                }
        )
    }
}

@Composable
private fun StayListItem(stay: ScheduleItemScreenModel, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(Color.Red, CircleShape),
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
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column {
                Text("숙소", fontSize = 13.sp, color = Color.Gray)
                Text(stay.scheduleName, fontSize = 15.sp, color = Color.Black)
            }
        }
        Spacer(modifier = Modifier.width(40.dp))
    }
}

private fun createCustomMarkerBitmap(context: Context, text: String, isAccommodation: Boolean): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (24 * density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = if (isAccommodation) android.graphics.Color.parseColor("#FFC107")
    else android.graphics.Color.parseColor("#14B8A6")
    paint.style = Paint.Style.FILL
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    if (isAccommodation) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.stay_icon)
        if (drawable != null) {
            val padding = (2 * density).toInt()
            val iconSize = size - (padding * 2)
            val left = (size - iconSize) / 2
            val top = (size - iconSize) / 2
            drawable.setBounds(left, top, left + iconSize, top + iconSize)
            drawable.setTintList(null)
            drawable.draw(canvas)
        }
    } else {
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 13 * density
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val y = (size / 2f) + (textBounds.height() / 2f) - (0.5f * density)
        canvas.drawText(text, size / 2f, y, paint)
    }

    return bitmap
}

class ScheduleDragDropState(
    val listState: LazyListState,
    private val onMoveRequest: (Int, Int) -> Unit
) {
    var draggedId by mutableStateOf<String?>(null)
        private set
    var dragOffsetY by mutableStateOf(0f)
        private set

    fun onDragStart(id: String) {
        draggedId = id
        dragOffsetY = 0f
    }

    fun onDrag(dragAmountY: Float, currentSchedules: List<ScheduleItemScreenModel>) {
        dragOffsetY += dragAmountY
        val currentDraggedId = draggedId ?: return

        val currentIndex = currentSchedules.indexOfFirst { it.scheduleId == currentDraggedId }
        val currentItemInfo = listState.layoutInfo.visibleItemsInfo.find { it.key == currentDraggedId }

        if (currentItemInfo != null && currentIndex != -1) {
            val visualCenterY = currentItemInfo.offset + (currentItemInfo.size / 2) + dragOffsetY

            val targetItemInfo = listState.layoutInfo.visibleItemsInfo.find {
                it.key != currentDraggedId &&
                        it.key != "header" &&
                        it.key != "stay" &&
                        visualCenterY >= it.offset && visualCenterY <= (it.offset + it.size)
            }

            if (targetItemInfo != null) {
                val targetIndex = currentSchedules.indexOfFirst { it.scheduleId == targetItemInfo.key }

                if (targetIndex != -1 && currentIndex != targetIndex) {
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
                        onMoveRequest(currentIndex, targetIndex)
                        dragOffsetY -= (totalShiftPx * direction)
                    }
                }
            }
        }
    }

    fun onDragInterrupted() {
        draggedId = null
        dragOffsetY = 0f
    }
}

@Composable
fun rememberScheduleDragDropState(
    listState: LazyListState = rememberLazyListState(),
    onMoveRequest: (Int, Int) -> Unit
): ScheduleDragDropState {
    return remember(listState) {
        ScheduleDragDropState(listState, onMoveRequest)
    }
}

// ===================== Preview =====================

@Preview(showBackground = true)
@Composable
fun ScheduleEditScreenPreview() {
    val scheduleList = listOf(
        ScheduleItemPresentationModel(scheduleId = "1", order = 1, scheduleName = "가덕휴게소", latitude = 35.024, longitude = 128.825),
        ScheduleItemPresentationModel(scheduleId = "2", order = 2, scheduleName = "매미성", latitude = 34.975, longitude = 128.718),
        ScheduleItemPresentationModel(scheduleId = "3", order = 3, scheduleName = "바람의 언덕", latitude = 34.761, longitude = 128.659),
        ScheduleItemPresentationModel(scheduleId = "4", order = 4, scheduleName = "거제 파노라마 케이블카", latitude = 34.801, longitude = 128.623)
    )
    val stayItem = ScheduleItemPresentationModel(
        scheduleId = "stay1", order = 0, scheduleName = "거제 YAHO HOTEL",
        latitude = 34.880, longitude = 128.621
    )

    ScheduleEditScreen(
        state = ScheduleEditState(
            dayNumber = 1,
            dateLabel = "8/30 (일)",
            schedules = scheduleList,
            stay = stayItem
        ),
        sharedState = PlanSharedState(),
        onIntent = {},
        onSharedIntent = {}
    )
}

data class ScheduleItemScreenModel(
    val scheduleId: String = "",
    val order: Int = 0,
    val scheduleName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

fun ScheduleItemPresentationModel.toScreen(): ScheduleItemScreenModel {
    return ScheduleItemScreenModel(
        scheduleId = this.scheduleId,
        order = this.order,
        scheduleName = this.scheduleName,
        latitude = this.latitude,
        longitude = this.longitude
    )
}

fun ScheduleItemScreenModel.toPresentation(): ScheduleItemPresentationModel {
    return ScheduleItemPresentationModel(
        scheduleId = this.scheduleId,
        order = this.order,
        scheduleName = this.scheduleName,
        latitude = this.latitude,
        longitude = this.longitude
    )
}