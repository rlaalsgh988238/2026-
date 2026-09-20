package com.braveberry.tourdataproject.screen.plan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.braveberry.tourdataproject.R
import com.braveberry.tourdataproject.ui.theme.BackgroundGray
import com.braveberry.tourdataproject.ui.theme.DisabledGray
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedState
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.RegionSelectionViewModel
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionPresentationModel
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionEffect
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionIntent
import com.tourdataproject.presentation.viewmodel.plan.regionSelect.uiState.RegionSelectionState

@Composable
fun RegionSelectionRoute(
    sharedViewModel: PlanSharedViewModel,
    viewModel: RegionSelectionViewModel = hiltViewModel(),
    onNavigateToDateSelection: () -> Unit,
    onNavigateBack: () -> Unit,
    isEditMode: Boolean = false,
    editCourseId: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentNavigateBack by rememberUpdatedState(onNavigateBack)
    val currentNavigateNext by rememberUpdatedState(onNavigateToDateSelection)

    LaunchedEffect(
        isEditMode,
        editCourseId,
        sharedState.course.courseId,
        sharedState.course.destination,
        sharedState.isCourseLoading,
        sharedState.courseLoadError
    ) {
        if (
            isEditMode &&
            !sharedState.isCourseLoading &&
            sharedState.courseLoadError == null &&
            sharedState.course.courseId == editCourseId
        ) {
            viewModel.onIntent(
                RegionSelectionIntent.OnInitializeEdit(
                    courseId = sharedState.course.courseId,
                    cityName = sharedState.course.destination
                )
            )
        }
    }

    LaunchedEffect(
        viewModel,
        sharedViewModel,
        lifecycleOwner
    ) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    RegionSelectionEffect.NavigateBack -> {
                        currentNavigateBack()
                    }

                    is RegionSelectionEffect.ApplyEditedDestination -> {
                        val success = sharedViewModel.applyEditedDestination(
                            courseId = effect.courseId,
                            cityName = effect.cityName,
                            latitude = effect.latitude,
                            longitude = effect.longitude
                        )

                        // 戻ってきたときにも再度操作できるように、
                        // 画面遷移前に送信状態を解除。
                        viewModel.onIntent(
                            RegionSelectionIntent.OnEditApplyCompleted(success)
                        )

                        if (success) {
                            currentNavigateNext()
                        }
                    }
                }
            }
        }
    }

    BackHandler {
        viewModel.onIntent(RegionSelectionIntent.OnBackButtonClicked)
    }

    RegionSelectionScreen(
        state = state,
        sharedState = sharedState,
        onSharedIntent = sharedViewModel::onIntent,
        onIntent = viewModel::onIntent,
        onNavigateNext = {
            if (isEditMode) {
                viewModel.onIntent(
                    RegionSelectionIntent.OnEditNextClicked
                )
            } else {
                // 생성 모드는 도시 선택 시 이미 좌표를 요청하므로
                // 동일한 좌표를 여기서 중복 요청하지 않음.
                onNavigateToDateSelection()
            }
        },
        isEditMode = isEditMode,
        onRetryCourseLoad = {
            editCourseId?.let { id ->
                sharedViewModel.onIntent(
                    PlanSharedIntent.OnLoadCourseById(id)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionScreen(
    state: RegionSelectionState,
    sharedState: PlanSharedState,
    onSharedIntent: (PlanSharedIntent) -> Unit,
    onIntent: (RegionSelectionIntent) -> Unit,
    onNavigateNext: () -> Unit,
    isEditMode: Boolean = false,
    onRetryCourseLoad: () -> Unit = {}
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    val courseLoadError = sharedState.courseLoadError

    val selectedCity = if (isEditMode) {
        state.editSelectedCity
    } else {
        sharedState.course.destination
    }

    val isReady = !isEditMode || (
            state.isEditInitialized &&
                    !sharedState.isCourseLoading &&
                    sharedState.courseLoadError == null
            )

    val canInteract = isReady && !state.isSubmitting

    val onCitySelected: (RegionPresentationModel) -> Unit = { region ->
        keyboardController?.hide()

        if (isEditMode) {
            onIntent(
                RegionSelectionIntent.OnEditCitySelected(region.exactName)
            )
        } else {
            onSharedIntent(
                PlanSharedIntent.OnCitySelected(region.exactName)
            )
            onIntent(
                RegionSelectionIntent.OnSearchQueryChanged("")
            )
        }
    }

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isEditMode) {
                                "여행 도시 변경"
                            } else {
                                "플랜 만들기"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            enabled = !state.isSubmitting,
                            onClick = {
                                keyboardController?.hide()
                                onIntent(
                                    RegionSelectionIntent.OnBackButtonClicked
                                )
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    R.drawable.arrow_circle_left
                                ),
                                contentDescription = "뒤로가기",
                                modifier = Modifier.size(40.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                HorizontalDivider(
                    color = Color(0xFFF0F0F0),
                    thickness = 1.dp
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                HorizontalDivider(
                    color = Color(0xFFF0F0F0),
                    thickness = 1.dp
                )

                Column(
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    )
                ) {
                    if (selectedCity.isNotBlank() && isReady) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isEditMode) {
                                    Color(0xFFE0E0E0)
                                } else {
                                    PrimaryTeal
                                }
                            ),
                            color = if (isEditMode) {
                                Color.White
                            } else {
                                PrimaryTeal.copy(alpha = 0.1f)
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable(
                                        enabled = canInteract,
                                        onClick = {
                                            if (isEditMode) {
                                                onIntent(
                                                    RegionSelectionIntent.OnEditCityDeselected
                                                )
                                            } else {
                                                onSharedIntent(
                                                    PlanSharedIntent.OnCityDeselected
                                                )
                                            }
                                        }
                                    )
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    )
                            ) {
                                val chipColor = if (isEditMode) {
                                    Color(0xFF333333)
                                } else {
                                    PrimaryTeal
                                }

                                Text(
                                    text = selectedCity,
                                    color = chipColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(Modifier.width(4.dp))

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "선택 취소",
                                    tint = chipColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    state.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            onNavigateNext()
                        },
                        enabled = canInteract && selectedCity.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            disabledContainerColor = DisabledGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                text = "다음",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
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
            Spacer(Modifier.height(16.dp))

            Text(
                text = "어디로 떠나시나요?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(20.dp))

            TextField(
                value = state.searchQuery,
                onValueChange = {
                    onIntent(
                        RegionSelectionIntent.OnSearchQueryChanged(it)
                    )
                },
                enabled = canInteract,
                placeholder = {
                    Text(
                        text = "도시 이름을 입력해주세요",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(
                            enabled = canInteract,
                            onClick = {
                                keyboardController?.hide()
                                onIntent(
                                    RegionSelectionIntent.OnSearchQueryChanged("")
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "지우기",
                                tint = Color.Gray
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BackgroundGray,
                    unfocusedContainerColor = BackgroundGray,
                    disabledContainerColor = BackgroundGray,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = PrimaryTeal
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isEditMode && courseLoadError != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                        ) {
                            Text(
                                text = courseLoadError,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                            TextButton(onClick = onRetryCourseLoad) {
                                Text(
                                    text = "다시 시도",
                                    color = PrimaryTeal
                                )
                            }
                        }
                    }

                    !isReady -> {
                        RegionLoadingContent(
                            message = "여행 정보를 불러오고 있습니다"
                        )
                    }

                    state.isSearchMode -> {
                        SearchResultList(
                            results = state.searchResults,
                            isSearching = state.isSearching,
                            enabled = canInteract,
                            onCityClick = onCitySelected
                        )
                    }

                    state.isLoading -> {
                        RegionLoadingContent(
                            message = "도시 정보를 가져오고 있습니다"
                        )
                    }

                    state.popularCities.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                        ) {
                            Text(
                                text = "도시 목록을 불러오지 못했습니다",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                            TextButton(
                                onClick = {
                                    onIntent(
                                        RegionSelectionIntent.OnRetryPopularCities
                                    )
                                }
                            ) {
                                Text(
                                    text = "다시 시도",
                                    color = PrimaryTeal
                                )
                            }
                        }
                    }

                    else -> {
                        PopularCityGrid(
                            cities = state.popularCities,
                            selectedCity = selectedCity,
                            enabled = canInteract,
                            onCityClick = onCitySelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionLoadingContent(
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = PrimaryTeal,
            modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun SearchResultList(
    results: List<RegionPresentationModel>,
    isSearching: Boolean,
    enabled: Boolean,
    onCityClick: (RegionPresentationModel) -> Unit
) {
    when {
        isSearching && results.isEmpty() -> {
            RegionLoadingContent("도시를 검색하고 있습니다")
        }

        results.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "검색 결과가 없습니다",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(results) { region ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = enabled,
                                onClick = { onCityClick(region) }
                            )
                            .padding(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = region.shortName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )

                            if (
                                region.city != null &&
                                region.province.isNotBlank()
                            ) {
                                Text(
                                    text = region.province,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularCityGrid(
    cities: List<RegionPresentationModel>,
    selectedCity: String?,
    enabled: Boolean,
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
                    color = if (isSelected) {
                        PrimaryTeal
                    } else {
                        Color(0xFFE0E0E0)
                    }
                ),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .clip(CircleShape)
                    .clickable(
                        enabled = enabled,
                        onClick = { onCityClick(region) }
                    )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = region.shortName,
                        color = if (isSelected) {
                            PrimaryTeal
                        } else {
                            Color.Black
                        },
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(
    name = "여행 도시 변경",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
fun RegionEditScreenPreview() {
    val dummyCities = listOf(
        "서울", "대전", "청주", "인천",
        "수원", "대구", "부산", "전주",
        "광주", "나주", "제주", "거제"
    ).mapIndexed { index, name ->
        RegionPresentationModel(
            code = (index + 1).toString(),
            province = name
        )
    }

    RegionSelectionScreen(
        state = RegionSelectionState(
            popularCities = dummyCities,
            editCourseId = "preview-course",
            isEditInitialized = true,
            editSelectedCity = "거제"
        ),
        sharedState = PlanSharedState(),
        onSharedIntent = {},
        onIntent = {},
        onNavigateNext = {},
        isEditMode = true
    )
}

@Preview(
    name = "플랜 만들기",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
fun RegionSelectionScreenPreview() {
    val dummyCities = listOf(
        "서울", "대전", "청주", "인천",
        "수원", "대구", "부산", "전주",
        "광주", "나주", "제주", "거제"
    ).mapIndexed { index, name ->
        RegionPresentationModel(
            code = (index + 1).toString(),
            province = name
        )
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
