package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.braveberry.tourdataproject.ui.theme.BackgroundGray
import com.braveberry.tourdataproject.ui.theme.DisabledGray
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.braveberry.tourdataproject.screen.pop.LoadingPopUp
import com.tourdataproject.presentation.model.RegionUiModel
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedEvent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEvent
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionState
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.RegionSelectionViewModel

@Composable
fun RegionSelectionRoute(
    sharedViewModel: PlanSharedViewModel,
    viewModel: RegionSelectionViewModel = hiltViewModel(),
    onNavigateToDateSelection: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val effect = viewModel.effect

    LaunchedEffect(effect) {
        effect.collect { currentEffect ->
            when (currentEffect) {
                is RegionSelectionEffect.NavigateToDateSelection -> {
                    state.selectedCity?.let { region ->
                        val regionName = region.city ?: region.province
                        sharedViewModel.setEvent(PlanSharedEvent.OnCitySelected(regionName))
                    }
                    onNavigateToDateSelection()
                }
                is RegionSelectionEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    RegionSelectionScreen(
        state = state,
        onEvent = viewModel::setEvent,
        onSharedEvent = sharedViewModel::setEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionScreen(
    state: RegionSelectionState,
    onEvent: (RegionSelectionEvent) -> Unit,
    onSharedEvent: (PlanSharedEvent) -> Unit
) {
    if (state.isLoading) {
        LoadingPopUp(message = "도시 정보를 가져오고 있습니다")
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "플랜 만들기",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(RegionSelectionEvent.OnBackButtonClicked) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            // 하단 버튼 영역이 시스템 네비게이션 바에 가려지지 않도록 수정
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding() // 🌟 시스템 바 영역만큼 패딩 추가
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // 선택된 도시 칩 표시 영역
                if (state.selectedCity != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, PrimaryTeal),
                        color = PrimaryTeal.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onEvent(RegionSelectionEvent.OnCityDeselected) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = state.selectedCity!!.shortName, // 🌟 shortName 유지
                                color = PrimaryTeal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "선택 취소",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // 다음 버튼
                Button(
                    onClick = { onEvent(RegionSelectionEvent.OnNextButtonClicked) },
                    enabled = state.isNextButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryTeal,
                        disabledContainerColor = DisabledGray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "다음",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "어디로 떠나시나요?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 검색 바
            TextField(
                value = state.searchQuery,
                onValueChange = { onEvent(RegionSelectionEvent.OnSearchQueryChanged(it)) },
                placeholder = {
                    Text("도시 이름을 입력해주세요", color = Color.Gray, fontSize = 14.sp)
                },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "검색", tint = Color.Gray)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BackgroundGray,
                    unfocusedContainerColor = BackgroundGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = PrimaryTeal
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 도시 선택 그리드
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.popularCities) { region ->
                    val isSelected = state.selectedCity == region

                    Surface(
                        shape = CircleShape,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) PrimaryTeal else Color(0xFFE0E0E0)
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                            .clip(CircleShape)
                            .clickable { onEvent(RegionSelectionEvent.OnCitySelected(region)) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = region.shortName, // 🌟 shortName 유지
                                color = if (isSelected) PrimaryTeal else Color.Black,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegionSelectionScreenPreview() {
    val dummyCities = listOf(
        "서울", "대전", "청주", "인천", "수원", "대구",
        "부산", "전주", "광주", "나주", "제주", "거제"
    ).mapIndexed { index, name ->
        RegionUiModel(code = (index + 1).toString(), province = name)
    }

    RegionSelectionScreen(
        state = RegionSelectionState(
            popularCities = dummyCities,
            selectedCity = dummyCities[11] // 거제가 선택된 상태
        ),
        onEvent = {},
        onSharedEvent = {}
    )
}
