package com.braveberry.tourdataproject.screen.plan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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
import com.tourdataproject.presentation.model.plan.AccessibilityInfoPresentationModel
import com.tourdataproject.presentation.model.plan.DayPlanPresentationModel
import com.tourdataproject.presentation.model.plan.ScheduleItemPresentationModel
import com.tourdataproject.presentation.model.plan.TravelCoursePresentationModel
import com.tourdataproject.presentation.utility.Log
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ===================== Route =====================

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

// ===================== Screen =====================

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

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 바텀시트 드래그 오프셋
    // 0 = EXPANDED(완전히 올라옴), maxOffset = COLLAPSED(peek만 보임)
    val offsetAnim = remember { Animatable(0f) }
    val maxOffsetRef = remember { mutableFloatStateOf(0f) }
    val isSheetMeasured = remember { mutableStateOf(false) }

    val navBarPx = with(density) {
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().toPx()
    }
    // peek = 드래그핸들(28dp) + 탭행(48dp) + 구분선(1dp) + 네비바
    val peekPx = with(density) { (28 + 48 + 1).dp.toPx() } + navBarPx

    var focusedLatLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    fun animCollapse() = scope.launch {
        offsetAnim.animateTo(maxOffsetRef.floatValue, tween(300))
    }
    fun animExpand() = scope.launch {
        offsetAnim.animateTo(0f, tween(300))
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Column {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_circle_left),
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 지도: 전체 영역 차지
            KakaoMap(
                focusedSchedules = allPlaces.map { it.toScreen() },
                stay = currentDayPlan?.stay?.takeIf { it.scheduleId.isNotBlank() }?.toScreen(),
                selectedScheduleId = selectedScheduleId,
                cameraFocusLatLng = focusedLatLng,
                onCameraFocusConsumed = { focusedLatLng = null }
            )

            // 바텀시트: 높이 35% 고정
            // fillMaxHeight(0.35f) → 콘텐츠 변화와 무관하게 항상 동일한 높이
            // offset으로 COLLAPSED(peek) ↔ EXPANDED(0) 전환
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .onGloballyPositioned { coords ->
                        // 최초 1회만: 실제 시트 높이로 maxOffset 계산 후 COLLAPSED 위치로 snap
                        if (!isSheetMeasured.value && coords.size.height > 0) {
                            isSheetMeasured.value = true
                            val sheetH = coords.size.height.toFloat()
                            val newMax = maxOf(0f, sheetH - peekPx)
                            maxOffsetRef.floatValue = newMax
                            scope.launch { offsetAnim.snapTo(newMax) }
                        }
                    }
                    .offset { IntOffset(0, offsetAnim.value.roundToInt()) },
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // 드래그 핸들 (28dp 고정)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        scope.launch { offsetAnim.stop() }
                                    },
                                    onVerticalDrag = { _, dy ->
                                        scope.launch {
                                            offsetAnim.snapTo(
                                                (offsetAnim.value + dy)
                                                    .coerceIn(0f, maxOffsetRef.floatValue)
                                            )
                                        }
                                    },
                                    onDragEnd = {
                                        if (offsetAnim.value < maxOffsetRef.floatValue / 2f) animExpand()
                                        else animCollapse()
                                    },
                                    onDragCancel = {
                                        if (offsetAnim.value < maxOffsetRef.floatValue / 2f) animExpand()
                                        else animCollapse()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
                        )
                    }

                    // 탭 행 (48dp 고정)
                    Box(modifier = Modifier.height(48.dp)) {
                        DayTabRow(
                            dayPlans = course.dayPlans,
                            selectedDayNumber = selectedDayNumber,
                            onDaySelected = { day -> onDaySelected(day) }
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // 콘텐츠 영역: 남은 공간 전부 차지 + LazyColumn 스크롤
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            selectedPlace != null -> {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        PlaceDetailSection(
                                            place = selectedPlace,
                                            dayLabel = currentDayPlan?.dayLabel ?: "",
                                            hasPrev = selectedIndex > 0,
                                            hasNext = selectedIndex < allPlaces.lastIndex,
                                            onPrev = {
                                                allPlaces[selectedIndex - 1].let {
                                                    onPlaceSelected(it.scheduleId)
                                                    focusedLatLng = it.latitude to it.longitude
                                                }
                                            },
                                            onNext = {
                                                allPlaces[selectedIndex + 1].let {
                                                    onPlaceSelected(it.scheduleId)
                                                    focusedLatLng = it.latitude to it.longitude
                                                }
                                            },
                                            onClose = onDetailClosed
                                        )
                                    }
                                    item {
                                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                                    }
                                }
                            }
                            allPlaces.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "등록된 일정이 없습니다",
                                        fontSize = 14.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    itemsIndexed(allPlaces) { index, place ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onPlaceSelected(place.scheduleId)
                                                    focusedLatLng = place.latitude to place.longitude
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = PrimaryTeal,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        "${index + 1}",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Text(place.scheduleName, fontSize = 15.sp)
                                        }
                                    }
                                    item {
                                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================== Sub Composables =====================

@Composable
private fun DayTabRow(
    dayPlans: List<DayPlanPresentationModel>,
    selectedDayNumber: Int,
    onDaySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
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
private fun PlaceDetailSection(
    place: ScheduleItemPresentationModel,
    dayLabel: String,
    hasPrev: Boolean,
    hasNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrev, enabled = hasPrev) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward_ios),
                    contentDescription = "이전",
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = 180f }
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
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 100.dp)
                    .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("사진", color = Color.Gray, fontSize = 13.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccessibilityRow(icon = R.drawable.elevator_icon, label = "엘리베이터")
                AccessibilityRow(icon = R.drawable.wheel_chair, label = "입구 경사로")
                AccessibilityRow(icon = R.drawable.wc, label = "장애인화장실 (남/여)")
                AccessibilityRow(icon = R.drawable.parking, label = "장애인 주차시설")
            }
        }
        Spacer(Modifier.height(8.dp))
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
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, color = Color.DarkGray)
    }
}

// ===================== KakaoMap =====================

@Composable
fun KakaoMap(
    focusedSchedules: List<ScheduleItemScreenModel>,
    stay: ScheduleItemScreenModel? = null,
    draggedId: String? = null,
    selectedScheduleId: String? = null,
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
                        override fun onMapReady(kakaoMap: KakaoMap) { mapInstance = kakaoMap }
                    }
                )
            }
        }
    )

    LaunchedEffect(focusedSchedules, stay, mapInstance, context, selectedScheduleId) {
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
            val isSelected = schedule.scheduleId == selectedScheduleId
            val cacheKey = if (isSelected) "selected_${schedule.scheduleId}" else "${index + 1}"
            val bitmap = bitmapCache.getOrPut(cacheKey) {
                if (isSelected) createLocationOnMarkerBitmap(context)
                else createCustomMarkerBitmap(context, "${index + 1}", isAccommodation = false)
            }
            val anchorY = if (isSelected) 1.0f else 0.5f
            val style = LabelStyles.from(LabelStyle.from(bitmap).setAnchorPoint(0.5f, anchorY))
            labelManager?.layer?.addLabel(LabelOptions.from(latLng).setStyles(style))
        }

        stay?.let {
            val stayLatLng = LatLng.from(it.latitude, it.longitude)
            points.add(stayLatLng)
            val bitmap = bitmapCache.getOrPut("accommodation") {
                createCustomMarkerBitmap(context, "", isAccommodation = true)
            }
            val style = LabelStyles.from(LabelStyle.from(bitmap).setAnchorPoint(0.5f, 0.5f))
            labelManager?.layer?.addLabel(LabelOptions.from(stayLatLng).setStyles(style))
        }

        if (points.size > 1) {
            points.add(points.first())
            val routeStyle = RouteLineStyle.from(4f, android.graphics.Color.parseColor("#888888"))
            val stylesSet = RouteLineStylesSet.from("route", RouteLineStyles.from(routeStyle))
            val segment = RouteLineSegment.from(points).setStyles(stylesSet.getStyles(0))
            routeLineManager?.layer?.addRouteLine(RouteLineOptions.from(segment))
        }

        if (!isInitialFocusDone) {
            val firstPoint = focusedSchedules.firstOrNull()
                ?.let { LatLng.from(it.latitude, it.longitude) }
                ?: stay?.let { LatLng.from(it.latitude, it.longitude) }
            firstPoint?.let {
                map.moveCamera(CameraUpdateFactory.newCenterPosition(it, 10))
                isInitialFocusDone = true
            }
        }
    }

    LaunchedEffect(cameraFocusLatLng) {
        val map = mapInstance ?: return@LaunchedEffect
        cameraFocusLatLng ?: return@LaunchedEffect
        val (lat, lng) = cameraFocusLatLng
        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(
                LatLng.from(lat, lng),
                map.cameraPosition?.zoomLevel ?: 15
            )
        )
        onCameraFocusConsumed()
    }

    LaunchedEffect(draggedId, mapInstance) {
        val map = mapInstance ?: return@LaunchedEffect
        draggedId ?: return@LaunchedEffect
        focusedSchedules.find { it.scheduleId == draggedId }?.let {
            map.moveCamera(
                CameraUpdateFactory.newCenterPosition(
                    LatLng.from(it.latitude, it.longitude),
                    map.cameraPosition?.zoomLevel ?: 10
                )
            )
        }
    }
}

// ===================== Bitmap Helpers =====================

private fun createLocationOnMarkerBitmap(context: Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val sizePx = (36 * density).toInt()
    val drawable = ContextCompat.getDrawable(context, R.drawable.location_on)
        ?: return Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(Canvas(bitmap))
    return bitmap
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
        ContextCompat.getDrawable(context, R.drawable.stay_icon)?.let { drawable ->
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

// ===================== Preview =====================

private fun mockCourse(): TravelCoursePresentationModel {
    val mockAccessibility = AccessibilityInfoPresentationModel()
    val mockSchedules = listOf(
        ScheduleItemPresentationModel(
            scheduleId = "1", order = 1, scheduleName = "가덕휴게소",
            latitude = 35.024, longitude = 128.825, accessibilityInfo = mockAccessibility
        ),
        ScheduleItemPresentationModel(
            scheduleId = "2", order = 2, scheduleName = "매미성",
            latitude = 34.975, longitude = 128.718, accessibilityInfo = mockAccessibility
        ),
        ScheduleItemPresentationModel(
            scheduleId = "3", order = 3, scheduleName = "학동흑진주몽돌해변",
            latitude = 34.761, longitude = 128.659, accessibilityInfo = mockAccessibility
        )
    )
    val mockStay = ScheduleItemPresentationModel(
        scheduleId = "stay1", order = 0, scheduleName = "거제 YAHO HOTEL",
        latitude = 34.880, longitude = 128.621
    )
    val mockDayPlans = listOf(
        DayPlanPresentationModel(
            dayLabel = "Day 1", dateLabel = "8/30", rawDayNumber = 1,
            rawDate = 0L, schedules = mockSchedules, stay = mockStay
        ),
        DayPlanPresentationModel(
            dayLabel = "Day 2", dateLabel = "8/31", rawDayNumber = 2,
            rawDate = 0L, schedules = emptyList(), stay = mockStay
        )
    )
    return TravelCoursePresentationModel(
        courseId = "course1",
        courseName = "거제 여행",
        datePeriod = "2026.08.30 ~ 2026.08.31",
        dayPlans = mockDayPlans
    )
}

@Preview(showBackground = true, name = "목록 상태")
@Composable
fun FullCourseMapScreenPreview() {
    FullCourseMapScreen(
        course = mockCourse(),
        selectedDayNumber = 1,
        selectedScheduleId = null,
        onDaySelected = {},
        onPlaceSelected = {},
        onDetailClosed = {},
        onBackClick = {}
    )
}

@Preview(showBackground = true, name = "상세 상태")
@Composable
fun FullCourseMapScreenDetailPreview() {
    FullCourseMapScreen(
        course = mockCourse(),
        selectedDayNumber = 1,
        selectedScheduleId = "1",
        onDaySelected = {},
        onPlaceSelected = {},
        onDetailClosed = {},
        onBackClick = {}
    )
}

@Preview(showBackground = true, name = "빈 일차 상태")
@Composable
fun FullCourseMapScreenEmptyDayPreview() {
    FullCourseMapScreen(
        course = mockCourse(),
        selectedDayNumber = 2,
        selectedScheduleId = null,
        onDaySelected = {},
        onPlaceSelected = {},
        onDetailClosed = {},
        onBackClick = {}
    )
}