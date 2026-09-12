package com.braveberry.tourdataproject.screen.toilet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.tourdataproject.presentation.utility.Log
import com.braveberry.tourdataproject.R
import com.tourdataproject.presentation.viewmodel.toilet.NearbyToiletViewModel
import com.tourdataproject.presentation.viewmodel.toilet.uiState.NearbyToiletEffect
import com.tourdataproject.presentation.viewmodel.toilet.uiState.NearbyToiletIntent
import com.tourdataproject.presentation.viewmodel.toilet.uiState.NearbyToiletState
import com.tourdataproject.presentation.viewmodel.toilet.uiState.ToiletItemPresentationModel

@Composable
fun NearbyToiletListRoute(
    viewModel: NearbyToiletViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NearbyToiletEffect.NavigateBack -> onBackClick()
                is NearbyToiletEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is NearbyToiletEffect.NavigateToExternalMap -> {
                    navigateToExternalMap(
                        context = context,
                        startLat = effect.startLat,
                        startLng = effect.startLng,
                        destLat = effect.destLat,
                        destLng = effect.destLng
                    )
                }
            }
        }
    }

    NearbyToiletListScreen(
        uiState = uiState,
        onBackClick = { viewModel.onIntent(NearbyToiletIntent.OnBackClicked) },
        onGuideClick = { targetToilet ->
            viewModel.onIntent(NearbyToiletIntent.OnToiletGuideClicked(targetToilet))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyToiletListScreen(
    uiState: NearbyToiletState,
    onBackClick: () -> Unit,
    onGuideClick: (ToiletItemPresentationModel) -> Unit
) {
    var mapInstance by remember { mutableStateOf<KakaoMap?>(null) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("최단거리 화장실 안내", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        BottomSheetScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            scaffoldState = scaffoldState,
            sheetPeekHeight = 350.dp,
            sheetContainerColor = Color.White,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetDragHandle = { BottomSheetDefaults.DragHandle() },
            sheetContent = {
                // 바텀시트 내부 화장실 리스트
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF13B7A1))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        items(uiState.toilets) { toilet ->
                            ToiletListItem(
                                toilet = toilet,
                                onGuideClick = { onGuideClick(toilet) }
                            )
                            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            start(
                                object : MapLifeCycleCallback() {
                                    override fun onMapDestroy() { Log.d("KakaoMap", "지도 소멸됨") }
                                    override fun onMapError(error: Exception?) { Log.e("KakaoMap", "에러: ${error?.message}") }
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
            }
        }
    }

    LaunchedEffect(uiState.currentLocation, mapInstance) {
        val map = mapInstance
        val location = uiState.currentLocation
        if (location != null && map != null) {
            val position = LatLng.from(location.first, location.second)

            val cameraUpdate = CameraUpdateFactory.newCenterPosition(position)
            map.moveCamera(cameraUpdate, CameraAnimation.from(500))

            //TODO: 라벨 안보여서 수정 해야함
            val layer = map.labelManager?.layer
            layer?.removeAll()

            val style = com.kakao.vectormap.label.LabelStyles.from(
                com.kakao.vectormap.label.LabelStyle.from(R.drawable.accessible)
            )
            val options = com.kakao.vectormap.label.LabelOptions.from(position).setStyles(style)
            layer?.addLabel(options)
        }
    }
}

@Composable
fun ToiletListItem(
    toilet: ToiletItemPresentationModel,
    onGuideClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                painter = painterResource(id = R.drawable.accessible),
                contentDescription = "화장실 아이콘",
                tint = Color(0xFF13B7A1),
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = toilet.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${toilet.distance} · ${toilet.address}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onGuideClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13B7A1)),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(text = "안내", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun NearbyToiletListScreenPreview() {
    val mockToiletList = listOf(
        ToiletItemPresentationModel(
            name = "거제종합병원",
            distance = "90m",
            address = "경남 거제시 두모동",
            lat = 34.8806,
            lng = 128.6211
        ),
        ToiletItemPresentationModel(
            name = "거제주민센터",
            distance = "120m",
            address = "경남 거제시 두모동",
            lat = 34.8810,
            lng = 128.6215
        ),
        ToiletItemPresentationModel(
            name = "거제복지관",
            distance = "130m",
            address = "경남 거제시 두모동",
            lat = 34.8815,
            lng = 128.6220
        )
    )

    val mockState = NearbyToiletState(
        isLoading = false,
        currentLocation = Pair(34.8806, 128.6211),
        toilets = mockToiletList,
        errorMessage = null
    )

    NearbyToiletListScreen(
        uiState = mockState,
        onBackClick = {},
        onGuideClick = {}
    )
}
