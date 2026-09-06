package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.ui.theme.BackgroundGray
import com.braveberry.tourdataproject.ui.theme.DisabledGray
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.braveberry.tourdataproject.screen.pop.LoadingPopUp
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionPresentationModel
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedState
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionIntent
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionState
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.RegionSelectionViewModel

@Composable
fun RegionSelectionRoute(
    sharedViewModel: PlanSharedViewModel,
    viewModel: RegionSelectionViewModel = hiltViewModel(),
    onNavigateToDateSelection: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { currentEffect ->
            when (currentEffect) {
                is RegionSelectionEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    RegionSelectionScreen(
        state = state,
        sharedState = sharedState,
        onSharedIntent = sharedViewModel::onIntent,
        onIntent = viewModel::onIntent,
        onNavigateNext = onNavigateToDateSelection
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionScreen(
    state: RegionSelectionState,
    sharedState: PlanSharedState,
    onSharedIntent: (PlanSharedIntent) -> Unit,
    onIntent: (RegionSelectionIntent) -> Unit,
    onNavigateNext: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val selectedDestination = sharedState.course.destination
    val isCitySelected = selectedDestination.isNotBlank()

    if (state.isLoading) {
        LoadingPopUp(message = "도시 정보를 가져오고 있습니다")
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets.ime,
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
                    IconButton(onClick = {
                        keyboardController?.hide()
                        onIntent(RegionSelectionIntent.OnBackButtonClicked)
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (isCitySelected) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, PrimaryTeal),
                        color = PrimaryTeal.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onSharedIntent(PlanSharedIntent.OnCityDeselected) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = sharedState.course.destination,
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

                Button(
                    onClick = {
                        keyboardController?.hide()
                        onSharedIntent(PlanSharedIntent.OnGetCityPosition(sharedState.course.destination))
                        onNavigateNext()
                    },
                    enabled = isCitySelected,
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

            TextField(
                value = state.searchQuery,
                onValueChange = { onIntent(RegionSelectionIntent.OnSearchQueryChanged(it)) },
                placeholder = {
                    Text("도시 이름을 입력해주세요", color = Color.Gray, fontSize = 14.sp)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            keyboardController?.hide()
                            onIntent(RegionSelectionIntent.OnSearchQueryChanged(""))
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "지우기", tint = Color.Gray)
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "검색", tint = Color.Gray)
                    }
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

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (state.isSearchMode) {
                    SearchResultList(
                        results = state.searchResults,
                        isSearching = state.isSearching,
                        onCityClick = { region ->
                            keyboardController?.hide()
                            onSharedIntent(PlanSharedIntent.OnCitySelected(region.exactName))
                            onIntent(RegionSelectionIntent.OnSearchQueryChanged(""))
                        }
                    )
                } else {
                    PopularCityGrid(
                        cities = state.popularCities,
                        selectedCity = selectedDestination,
                        onCityClick = { region ->
                            keyboardController?.hide()
                            onSharedIntent(PlanSharedIntent.OnCitySelected(region.exactName))
                            onIntent(RegionSelectionIntent.OnSearchQueryChanged(""))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultList(
    results: List<RegionPresentationModel>,
    isSearching: Boolean,
    onCityClick: (RegionPresentationModel) -> Unit
) {
    when {
        isSearching && results.isEmpty() -> {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.size(28.dp))
            }
        }
        results.isEmpty() -> {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text("검색 결과가 없습니다", color = Color.Gray, fontSize = 14.sp)
            }
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(results) { region ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCityClick(region) }
                            .padding(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = region.shortName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            if (region.city != null && region.province.isNotBlank()) {
                                Text(
                                    text = region.province,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun PopularCityGrid(
    cities: List<RegionPresentationModel>,
    selectedCity: String?,
    onCityClick: (RegionPresentationModel) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cities) { region ->
            val isSelected = selectedCity == region.exactName

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
                    .clickable { onCityClick(region) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = region.shortName,
                        color = if (isSelected) PrimaryTeal else Color.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
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
        RegionPresentationModel(code = (index + 1).toString(), province = name)
    }

    RegionSelectionScreen(
        state = RegionSelectionState(
            popularCities = dummyCities
        ),
        sharedState = PlanSharedState(),
        onSharedIntent = {},
        onIntent = {},
        onNavigateNext = {}
    )
}
