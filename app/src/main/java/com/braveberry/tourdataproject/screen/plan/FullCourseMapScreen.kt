package com.braveberry.tourdataproject.screen.plan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val SheetItemBackground = Color(0xFFF6F6F6)
private val SheetDividerColor = Color(0xFFE5E5E5)
private val SheetSecondaryText = Color(0xFF888888)

// ===================== Route =====================

@Composable
fun FullCourseMapRoute(
    sharedViewModel: PlanSharedViewModel,
    onNavigateBack: () -> Unit
) {
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()
    val course = sharedState.course
    val dayPlans = course.dayPlans

    var selectedDayNumber by remember(course.courseId) {
        mutableIntStateOf(dayPlans.firstOrNull()?.rawDayNumber ?: 1)
    }
    var selectedScheduleId by remember(course.courseId) {
        mutableStateOf<String?>(null)
    }

    // 데이터가 늦게 들어오거나 일차가 변경된 경우 선택값 보정
    LaunchedEffect(dayPlans, selectedDayNumber, selectedScheduleId) {
        val selectedDay = dayPlans.find {
            it.rawDayNumber == selectedDayNumber
        }

        if (selectedDay == null) {
            selectedDayNumber = dayPlans.firstOrNull()?.rawDayNumber ?: 1
            selectedScheduleId = null
        } else if (
            selectedScheduleId != null &&
            selectedDay.schedules.none {
                it.scheduleId == selectedScheduleId
            }
        ) {
            selectedScheduleId = null
        }
    }

    FullCourseMapScreen(
        course = course,
        selectedDayNumber = selectedDayNumber,
        selectedScheduleId = selectedScheduleId,
        onDaySelected = {
            selectedDayNumber = it
            selectedScheduleId = null
        },
        onPlaceSelected = {
            selectedScheduleId = it
        },
        onDetailClosed = {
            selectedScheduleId = null
        },
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
    val currentDayPlan = course.dayPlans.find {
        it.rawDayNumber == selectedDayNumber
    }
    val allPlaces = currentDayPlan?.schedules.orEmpty()
    val selectedIndex = allPlaces.indexOfFirst {
        it.scheduleId == selectedScheduleId
    }
    val selectedPlace = allPlaces.getOrNull(selectedIndex)

    var focusedLatLng by remember {
        mutableStateOf<Pair<Double, Double>?>(null)
    }

    val selectedLatitude = selectedPlace?.latitude
    val selectedLongitude = selectedPlace?.longitude

    LaunchedEffect(
        selectedDayNumber,
        selectedScheduleId,
        selectedLatitude,
        selectedLongitude
    ) {
        focusedLatLng = if (
            selectedLatitude != null &&
            selectedLongitude != null
        ) {
            selectedLatitude to selectedLongitude
        } else {
            null
        }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CourseMapTopBar(
                courseName = course.courseName,
                datePeriod = course.datePeriod,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clipToBounds()
        ) {
            KakaoMap(
                focusedSchedules = allPlaces.map { it.toScreen() },
                stay = currentDayPlan?.stay
                    ?.takeIf { it.scheduleId.isNotBlank() }
                    ?.toScreen(),
                selectedScheduleId = selectedScheduleId,
                cameraFocusLatLng = focusedLatLng,
                onCameraFocusConsumed = {
                    focusedLatLng = null
                },
                cameraDayKey = selectedDayNumber
            )

            CourseMapBottomSheet(
                dayPlans = course.dayPlans,
                selectedDayNumber = selectedDayNumber,
                places = allPlaces,
                selectedPlace = selectedPlace,
                selectedIndex = selectedIndex,
                onDaySelected = onDaySelected,
                onPlaceSelected = {
                    onPlaceSelected(it.scheduleId)
                },
                onPrev = {
                    allPlaces.getOrNull(selectedIndex - 1)?.let {
                        onPlaceSelected(it.scheduleId)
                    }
                },
                onNext = {
                    allPlaces.getOrNull(selectedIndex + 1)?.let {
                        onPlaceSelected(it.scheduleId)
                    }
                },
                onDetailClosed = onDetailClosed
            )
        }
    }
}

@Composable
private fun CourseMapTopBar(
    courseName: String,
    datePeriod: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Spacer(
            Modifier.windowInsetsTopHeight(WindowInsets.statusBars)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_circle_left),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Unspecified
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = courseName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = datePeriod,
                    fontSize = 12.sp,
                    color = SheetSecondaryText
                )
            }
        }

        HorizontalDivider(
            color = Color(0xFFF0F0F0),
            thickness = 1.dp
        )
    }
}

// ===================== Bottom Sheet =====================

@Composable
private fun CourseMapBottomSheet(
    dayPlans: List<DayPlanPresentationModel>,
    selectedDayNumber: Int,
    places: List<ScheduleItemPresentationModel>,
    selectedPlace: ScheduleItemPresentationModel?,
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit,
    onPlaceSelected: (ScheduleItemPresentationModel) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onDetailClosed: () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var sheetOffsetPx by remember { mutableFloatStateOf(0f) }
    var settleJob by remember { mutableStateOf<Job?>(null) }

    val navigationBarPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        // 목록은 낮게, 상세는 편의시설을 볼 수 있도록 높게 표시
        val desiredHeight = if (selectedPlace == null) {
            248.dp
        } else {
            380.dp
        }

        val targetHeight = (desiredHeight + navigationBarPadding)
            .coerceAtMost(maxHeight * 0.85f)

        val sheetHeight by animateDpAsState(
            targetValue = targetHeight,
            animationSpec = tween(250),
            label = "course_sheet_height"
        )

        val peekHeight = 76.dp + navigationBarPadding

        val maxOffsetPx = with(density) {
            (sheetHeight - peekHeight)
                .coerceAtLeast(0.dp)
                .toPx()
        }

        val velocityThreshold = with(density) {
            700.dp.toPx()
        }

        fun settleSheet(target: Float) {
            settleJob?.cancel()
            settleJob = scope.launch {
                animate(
                    initialValue = sheetOffsetPx.coerceIn(0f, maxOffsetPx),
                    targetValue = target.coerceIn(0f, maxOffsetPx),
                    animationSpec = tween(220)
                ) { value, _ ->
                    sheetOffsetPx = value
                }
            }
        }

        // 일차 또는 장소가 바뀌면 내용을 볼 수 있도록 펼침
        LaunchedEffect(selectedDayNumber, selectedPlace?.scheduleId) {
            settleJob?.cancel()
            sheetOffsetPx = 0f
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(sheetHeight)
                .offset {
                    IntOffset(
                        x = 0,
                        y = sheetOffsetPx
                            .coerceIn(0f, maxOffsetPx)
                            .roundToInt()
                    )
                },
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            ),
            color = Color.White,
            border = BorderStroke(1.dp, SheetDividerColor),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 드래그 핸들: 탭으로도 접기/펼치기 가능
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .draggable(
                            state = rememberDraggableState { delta ->
                                sheetOffsetPx = (sheetOffsetPx + delta)
                                    .coerceIn(0f, maxOffsetPx)
                            },
                            orientation = Orientation.Vertical,
                            onDragStarted = {
                                settleJob?.cancel()
                            },
                            onDragStopped = { velocity ->
                                val shouldCollapse = when {
                                    velocity > velocityThreshold -> true
                                    velocity < -velocityThreshold -> false
                                    else -> sheetOffsetPx > maxOffsetPx / 2f
                                }

                                settleSheet(
                                    if (shouldCollapse) maxOffsetPx else 0f
                                )
                            }
                        )
                        .clickable {
                            settleSheet(
                                if (sheetOffsetPx > maxOffsetPx / 2f) {
                                    0f
                                } else {
                                    maxOffsetPx
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(4.dp)
                            .background(
                                color = Color(0xFFABABAB),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                DayTabRow(
                    dayPlans = dayPlans,
                    selectedDayNumber = selectedDayNumber,
                    onDaySelected = { dayNumber ->
                        settleJob?.cancel()
                        sheetOffsetPx = 0f
                        onDaySelected(dayNumber)

                        // 현재 일차 탭을 눌러도 상세에서 목록으로 복귀
                        if (selectedPlace != null) {
                            onDetailClosed()
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                when {
                    selectedPlace != null -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp + navigationBarPadding
                            )
                        ) {
                            item {
                                PlaceDetailSection(
                                    place = selectedPlace,
                                    hasPrev = selectedIndex > 0,
                                    hasNext = selectedIndex < places.lastIndex,
                                    onPrev = onPrev,
                                    onNext = onNext,
                                    onClose = onDetailClosed
                                )
                            }
                        }
                    }

                    places.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = navigationBarPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "등록된 일정이 없습니다",
                                color = SheetSecondaryText,
                                fontSize = 14.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 16.dp + navigationBarPadding
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                items = places,
                                key = { _, place -> place.scheduleId }
                            ) { index, place ->
                                CoursePlaceRow(
                                    number = index + 1,
                                    placeName = place.scheduleName,
                                    onClick = {
                                        onPlaceSelected(place)
                                    }
                                )
                            }
                        }
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
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayPlans.forEach { dayPlan ->
            val selected = dayPlan.rawDayNumber == selectedDayNumber

            Surface(
                onClick = {
                    onDaySelected(dayPlan.rawDayNumber)
                },
                shape = RoundedCornerShape(10.dp),
                color = if (selected) {
                    PrimaryTeal.copy(alpha = 0.16f)
                } else {
                    Color(0xFFE2E2E2)
                },
                border = if (selected) {
                    BorderStroke(
                        width = 1.dp,
                        color = PrimaryTeal.copy(alpha = 0.45f)
                    )
                } else {
                    null
                }
            ) {
                Text(
                    text = dayPlan.dayLabel,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    color = if (selected) {
                        PrimaryTeal
                    } else {
                        SheetSecondaryText
                    },
                    fontSize = 12.sp,
                    fontWeight = if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )
            }
        }
    }
}

@Composable
private fun CoursePlaceRow(
    number: Int,
    placeName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = PrimaryTeal
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 36.dp)
                .background(
                    color = SheetItemBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = placeName,
                fontSize = 14.sp,
                color = Color(0xFF222222),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ===================== Place Detail =====================

@Composable
private fun PlaceDetailSection(
    place: ScheduleItemPresentationModel,
    hasPrev: Boolean,
    hasNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    val accessibility = place.accessibilityInfo

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceNavigationButton(
                isPrevious = true,
                enabled = hasPrev,
                onClick = onPrev
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.scheduleName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = place.address
                        .orEmpty()
                        .ifBlank { "주소 정보 없음" },
                    fontSize = 11.sp,
                    color = SheetSecondaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            PlaceNavigationButton(
                isPrevious = false,
                enabled = hasNext,
                onClick = onNext
            )
        }

        Spacer(Modifier.height(10.dp))

        HorizontalDivider(
            color = SheetDividerColor,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "편의시설 정보",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )

            Text(
                text = "목록 보기",
                fontSize = 12.sp,
                color = PrimaryTeal,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onClose)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        // 실제 저장된 정보 표시. 값이 없으면 시설이 있다고 단정하지 않음.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccessibilityCard(
                icon = R.drawable.elevator_icon,
                label = "엘리베이터",
                description = accessibilityDescription(accessibility?.elevator),
                modifier = Modifier.weight(1f)
            )

            AccessibilityCard(
                icon = R.drawable.wc,
                label = "장애인 화장실",
                description = accessibilityDescription(accessibility?.restroom),
                modifier = Modifier.weight(1f)
            )

            AccessibilityCard(
                icon = R.drawable.wheel_chair,
                label = "입구 경사로",
                description = accessibilityDescription(accessibility?.exit),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccessibilityCard(
                icon = R.drawable.parking,
                label = "장애인 주차시설",
                description = accessibilityDescription(accessibility?.parking),
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PlaceNavigationButton(
    isPrevious: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.arrow_forward_ios),
            contentDescription = if (isPrevious) "이전 장소" else "다음 장소",
            tint = if (enabled) {
                PrimaryTeal
            } else {
                Color(0xFFCACACA)
            },
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer {
                    rotationZ = if (isPrevious) 180f else 0f
                }
        )
    }
}

@Composable
private fun AccessibilityCard(
    icon: Int,
    label: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = SheetItemBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            // 설명 유무와 관계없이 동일한 색상
            tint = Color(0xFF242424)
        )

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center
        )

        Text(
            text = description
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "세부정보 없음",
            fontSize = 10.sp,
            lineHeight = 14.sp,
            color = SheetSecondaryText,
            textAlign = TextAlign.Center
        )
    }
}

private fun accessibilityDescription(value: Any?): String? {
    return value
        ?.toString()
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}

// ===================== Kakao Map =====================

@Composable
fun KakaoMap(
    focusedSchedules: List<ScheduleItemScreenModel>,
    stay: ScheduleItemScreenModel? = null,
    draggedId: String? = null,
    selectedScheduleId: String? = null,
    cameraFocusLatLng: Pair<Double, Double>? = null,
    onCameraFocusConsumed: () -> Unit = {},
    cameraDayKey: Int? = null
) {
    if (LocalInspectionMode.current) {
        MapPreviewBackground(
            placeCount = focusedSchedules.size,
            selectedIndex = focusedSchedules.indexOfFirst {
                it.scheduleId == selectedScheduleId
            },
            hasStay = stay != null
        )
        return
    }

    var mapInstance by remember {
        mutableStateOf<KakaoMap?>(null)
    }

    val context = LocalContext.current
    val bitmapCache = remember {
        mutableMapOf<String, Bitmap>()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {
                            Log.d("KakaoMap", "지도 소멸됨")
                        }

                        override fun onMapError(error: Exception?) {
                            Log.e(
                                "KakaoMap",
                                "지도 에러: ${error?.message}"
                            )
                        }
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

    // 일정 또는 선택 상태가 변경되면 마커 갱신
    LaunchedEffect(
        focusedSchedules,
        stay,
        mapInstance,
        selectedScheduleId
    ) {
        val map = mapInstance ?: return@LaunchedEffect
        val labelLayer = map.labelManager?.layer
        val routeLayer = map.routeLineManager?.layer

        // 빈 일차에서도 이전 마커가 남지 않도록 먼저 삭제
        labelLayer?.removeAll()
        routeLayer?.removeAll()

        val points = mutableListOf<LatLng>()

        focusedSchedules.forEachIndexed { index, schedule ->
            val number = index + 1
            val latLng = LatLng.from(
                schedule.latitude,
                schedule.longitude
            )
            val isSelected = schedule.scheduleId == selectedScheduleId

            points.add(latLng)

            val cacheKey = if (isSelected) {
                "selected_$number"
            } else {
                "normal_$number"
            }

            val bitmap = bitmapCache.getOrPut(cacheKey) {
                if (isSelected) {
                    createSelectedMarkerBitmap(
                        context = context,
                        text = number.toString()
                    )
                } else {
                    createCustomMarkerBitmap(
                        context = context,
                        text = number.toString(),
                        isAccommodation = false
                    )
                }
            }

            val style = LabelStyles.from(
                LabelStyle.from(bitmap)
                    .setAnchorPoint(
                        0.5f,
                        if (isSelected) 1f else 0.5f
                    )
            )

            labelLayer?.addLabel(
                LabelOptions.from(latLng).setStyles(style)
            )
        }

        stay?.let { accommodation ->
            val latLng = LatLng.from(
                accommodation.latitude,
                accommodation.longitude
            )

            points.add(latLng)

            val bitmap = bitmapCache.getOrPut("accommodation") {
                createCustomMarkerBitmap(
                    context = context,
                    text = "",
                    isAccommodation = true
                )
            }

            val style = LabelStyles.from(
                LabelStyle.from(bitmap)
                    .setAnchorPoint(0.5f, 0.5f)
            )

            labelLayer?.addLabel(
                LabelOptions.from(latLng).setStyles(style)
            )
        }

        if (points.size > 1) {
            // 마지막 장소를 첫 장소와 강제로 연결하지 않음
            val routeStyle = RouteLineStyle.from(
                4f,
                android.graphics.Color.parseColor("#888888")
            )

            val stylesSet = RouteLineStylesSet.from(
                "route",
                RouteLineStyles.from(routeStyle)
            )

            val segment = RouteLineSegment
                .from(points)
                .setStyles(stylesSet.getStyles(0))

            routeLayer?.addRouteLine(
                RouteLineOptions.from(segment)
            )
        }
    }

    // 일정 구성이 바뀌면 해당 일차의 첫 장소로 이동
    val schedulePositions = focusedSchedules.map {
        Triple(it.scheduleId, it.latitude, it.longitude)
    }
    val stayPosition = stay?.let {
        Triple(it.scheduleId, it.latitude, it.longitude)
    }

    // 첫 진입·일차 변경: 줌 레벨 13
    LaunchedEffect(
        cameraDayKey,
        schedulePositions,
        stayPosition,
        mapInstance
    ) {
        val map = mapInstance ?: return@LaunchedEffect

        // 상세 선택 중에는 초기 카메라 이동으로 덮어쓰지 않음
        if (selectedScheduleId != null) return@LaunchedEffect

        val firstPoint = focusedSchedules.firstOrNull()?.let {
            LatLng.from(it.latitude, it.longitude)
        } ?: stay?.let {
            LatLng.from(it.latitude, it.longitude)
        }

        firstPoint?.let {
            map.moveCamera(
                CameraUpdateFactory.newCenterPosition(it, 13)
            )
        }
    }

    // 지도 준비 전에 들어온 선택 요청도 준비 완료 후 처리
    // 선택한 장소를 화면 중앙보다 위쪽에 표시
    // 일정 상세 선택: 줌 레벨 14
    LaunchedEffect(cameraFocusLatLng, mapInstance) {
        val map = mapInstance ?: return@LaunchedEffect
        val target = cameraFocusLatLng ?: return@LaunchedEffect

        // 마커가 바텀시트 위에 보이도록 중심을 남쪽으로 보정
        // 15 → 14로 축소한 만큼 기존 보정값도 늘림
        val latitudeOffset = 0.008

        map.moveCamera(
            CameraUpdateFactory.newCenterPosition(
                LatLng.from(
                    target.first - latitudeOffset,
                    target.second
                ),
                14
            )
        )

        onCameraFocusConsumed()
    }


    LaunchedEffect(draggedId, mapInstance, focusedSchedules) {
        val map = mapInstance ?: return@LaunchedEffect
        val targetId = draggedId ?: return@LaunchedEffect

        focusedSchedules.find {
            it.scheduleId == targetId
        }?.let {
            map.moveCamera(
                CameraUpdateFactory.newCenterPosition(
                    LatLng.from(it.latitude, it.longitude),
                    map.cameraPosition?.zoomLevel ?: 15
                )
            )
        }
    }
}

// ===================== Bitmap Helpers =====================

private fun createSelectedMarkerBitmap(
    context: Context,
    text: String
): Bitmap {
    val density = context.resources.displayMetrics.density
    val width = (36 * density).roundToInt().coerceAtLeast(1)
    val height = (46 * density).roundToInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val centerX = width / 2f
    val circleY = 17 * density
    val radius = 15 * density

    val path = android.graphics.Path().apply {
        moveTo(centerX - 12 * density, 25 * density)

        cubicTo(
            centerX - 9 * density,
            32 * density,
            centerX - 3 * density,
            40 * density,
            centerX,
            44 * density
        )

        cubicTo(
            centerX + 3 * density,
            40 * density,
            centerX + 9 * density,
            32 * density,
            centerX + 12 * density,
            25 * density
        )

        close()
    }

    paint.color = android.graphics.Color.parseColor("#14B8A6")
    paint.style = Paint.Style.FILL

    canvas.drawPath(path, paint)
    canvas.drawCircle(centerX, circleY, radius, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 17 * density
    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textAlign = Paint.Align.CENTER

    val baseline = circleY -
            (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f

    canvas.drawText(
        text,
        centerX,
        baseline,
        paint
    )

    return bitmap
}

private fun createCustomMarkerBitmap(
    context: Context,
    text: String,
    isAccommodation: Boolean
): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (24 * density).roundToInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(
        size,
        size,
        Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.color = if (isAccommodation) {
        android.graphics.Color.parseColor("#FFC107")
    } else {
        android.graphics.Color.parseColor("#14B8A6")
    }

    paint.style = Paint.Style.FILL

    canvas.drawCircle(
        size / 2f,
        size / 2f,
        size / 2f,
        paint
    )

    if (isAccommodation) {
        ContextCompat.getDrawable(
            context,
            R.drawable.stay_icon
        )?.mutate()?.let { drawable ->
            val padding = (2 * density).roundToInt()
            val iconSize = size - padding * 2
            val left = (size - iconSize) / 2
            val top = (size - iconSize) / 2

            drawable.setBounds(
                left,
                top,
                left + iconSize,
                top + iconSize
            )
            drawable.setTintList(null)
            drawable.draw(canvas)
        }
    } else {
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 13 * density
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.DEFAULT_BOLD

        val textBounds = Rect()
        paint.getTextBounds(
            text,
            0,
            text.length,
            textBounds
        )

        val baseline = size / 2f -
                (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f

        canvas.drawText(
            text,
            size / 2f,
            baseline,
            paint
        )
    }

    return bitmap
}

// ===================== Preview Map =====================

@Composable
private fun MapPreviewBackground(
    placeCount: Int,
    selectedIndex: Int,
    hasStay: Boolean
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5F0E8))
    ) {
        val mapWidth = maxWidth
        val mapHeight = maxHeight

        // 실제 지도가 아닌 프리뷰용 대체 배경
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.38f)
                .fillMaxSize()
                .background(Color(0xFFC7E6F2))
        )

        Text(
            text = "지도 미리보기",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            fontSize = 12.sp,
            color = Color(0xFF70877C)
        )

        repeat(placeCount.coerceAtMost(6)) { index ->
            val xFraction = when (index % 3) {
                0 -> 0.48f
                1 -> 0.67f
                else -> 0.22f
            }
            val yFraction = when (index % 3) {
                0 -> 0.26f
                1 -> 0.16f
                else -> 0.35f
            }

            val selected = index == selectedIndex

            Surface(
                modifier = Modifier
                    .offset(
                        x = mapWidth * xFraction,
                        y = mapHeight * yFraction
                    )
                    .size(if (selected) 38.dp else 26.dp),
                shape = CircleShape,
                color = PrimaryTeal,
                border = BorderStroke(1.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (index + 1).toString(),
                        fontSize = if (selected) 18.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (hasStay) {
            Surface(
                modifier = Modifier
                    .offset(
                        x = mapWidth * 0.35f,
                        y = mapHeight * 0.43f
                    )
                    .size(28.dp),
                shape = CircleShape,
                color = Color(0xFFFFC107)
            ) {
                Icon(
                    painter = painterResource(R.drawable.stay_icon),
                    contentDescription = "숙소",
                    modifier = Modifier.padding(4.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

// ===================== Preview Data =====================

private fun mockCourse(): TravelCoursePresentationModel {
    val accessibility = AccessibilityInfoPresentationModel(
        elevator = "엘리베이터 있음",
        restroom = "장애인 화장실 있음",
        exit = "입구 경사로 있음",
        parking = "장애인 주차구역 있음"
    )

    val schedules = listOf(
        ScheduleItemPresentationModel(
            scheduleId = "1",
            order = 1,
            scheduleName = "가덕휴게소",
            address = "부산광역시 강서구",
            latitude = 35.024,
            longitude = 128.825,
            accessibilityInfo = accessibility
        ),
        ScheduleItemPresentationModel(
            scheduleId = "2",
            order = 2,
            scheduleName = "매미성",
            address = "경상남도 거제시 장목면",
            latitude = 34.975,
            longitude = 128.718,
            accessibilityInfo = accessibility
        ),
        ScheduleItemPresentationModel(
            scheduleId = "3",
            order = 3,
            scheduleName = "학동흑진주몽돌해변",
            address = "경상남도 거제시 동부면",
            latitude = 34.761,
            longitude = 128.659,
            accessibilityInfo = AccessibilityInfoPresentationModel()
        )
    )

    val stay = ScheduleItemPresentationModel(
        scheduleId = "stay1",
        order = 0,
        scheduleName = "거제 YAHO HOTEL",
        latitude = 34.880,
        longitude = 128.621
    )

    return TravelCoursePresentationModel(
        courseId = "course1",
        courseName = "거제 여행",
        datePeriod = "2026.08.30 ~ 2026.08.31",
        dayPlans = listOf(
            DayPlanPresentationModel(
                dayLabel = "Day 1",
                dateLabel = "8/30",
                rawDayNumber = 1,
                rawDate = 0L,
                schedules = schedules,
                stay = stay
            ),
            DayPlanPresentationModel(
                dayLabel = "Day 2",
                dateLabel = "8/31",
                rawDayNumber = 2,
                rawDate = 0L,
                schedules = emptyList(),
                stay = stay
            )
        )
    )
}

// ===================== Previews =====================

// 인터랙티브 프리뷰에서 탭과 장소 선택도 가능
@Composable
private fun CourseMapPreviewContent(
    initialDayNumber: Int = 1,
    initialScheduleId: String? = null
) {
    val course = remember { mockCourse() }

    var selectedDayNumber by remember {
        mutableIntStateOf(initialDayNumber)
    }
    var selectedScheduleId by remember {
        mutableStateOf(initialScheduleId)
    }

    MaterialTheme {
        FullCourseMapScreen(
            course = course,
            selectedDayNumber = selectedDayNumber,
            selectedScheduleId = selectedScheduleId,
            onDaySelected = {
                selectedDayNumber = it
                selectedScheduleId = null
            },
            onPlaceSelected = {
                selectedScheduleId = it
            },
            onDetailClosed = {
                selectedScheduleId = null
            },
            onBackClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "지도 - 목록"
)
@Composable
fun FullCourseMapScreenPreview() {
    CourseMapPreviewContent()
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "지도 - 장소 상세"
)
@Composable
fun FullCourseMapScreenDetailPreview() {
    CourseMapPreviewContent(
        initialScheduleId = "1"
    )
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "지도 - 편의시설 정보 없음"
)
@Composable
fun FullCourseMapScreenUnknownInfoPreview() {
    CourseMapPreviewContent(
        initialScheduleId = "3"
    )
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    name = "지도 - 빈 일차"
)
@Composable
fun FullCourseMapScreenEmptyDayPreview() {
    CourseMapPreviewContent(
        initialDayNumber = 2
    )
}

@Preview(
    showBackground = true,
    widthDp = 320,
    heightDp = 640,
    name = "지도 - 작은 화면 상세"
)
@Composable
fun FullCourseMapScreenSmallPreview() {
    CourseMapPreviewContent(
        initialScheduleId = "1"
    )
}
