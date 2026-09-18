package com.braveberry.tourdataproject.screen.kakaoMap

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.ui.theme.PrimaryTeal
import com.tourdataproject.presentation.KakaoMapEffect
import com.tourdataproject.presentation.KakaoMapIntent
import com.tourdataproject.presentation.model.KakaoMapPresentationModel
import com.tourdataproject.presentation.utility.ScreenPurpose
import com.tourdataproject.presentation.viewmodel.kakaoMap.KakaoMapViewModel
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun KakaoMapSearchRoute(
    sharedViewModel: PlanSharedViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    viewModel: KakaoMapViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToNext: () -> Unit,
    onNavigateToDateSelect: (purpose: String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiState by viewModel.container.stateFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(KakaoMapIntent.OnSearchQueryChanged(""))
        val courseState = sharedViewModel.sharedState.value
        val lat = courseState.course.destinationLatitude
        val lng = courseState.course.destinationLongitude
        if (lat != 0.0 && lng != 0.0) {
            viewModel.onIntent(KakaoMapIntent.OnInitLocation(lat, lng))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is KakaoMapEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is KakaoMapEffect.NavigateNextScreen -> {
                    when(state.purpose){
                        ScreenPurpose.ADD_STAY -> {
                            sharedViewModel.onIntent(PlanSharedIntent.OnSetDraftStay(effect.place))
                            onNavigateToDateSelect(ScreenPurpose.ADD_STAY)
                        }
                        ScreenPurpose.ADD_SCHEDULE -> {
                            sharedViewModel.onIntent(PlanSharedIntent.OnSetDraftSchedule(effect.place))
                            onNavigateToNext()
                        }
                    }
                }
            }
        }
    }

    val handleBackClick = {
        sharedViewModel.onIntent(PlanSharedIntent.OnClearDraftSchedule)
        onBackClick()
    }

    KakaoMapSearchScreen(
        modifier = modifier,
        searchQuery = uiState.searchQuery,
        isLoading = uiState.isLoading,
        searchResults = uiState.searchResults,
        autoCompleteResults = uiState.autoCompleteResults,
        onQueryChanged = { viewModel.onIntent(KakaoMapIntent.OnSearchQueryChanged(it)) },
        onSearch = { query -> viewModel.onIntent(KakaoMapIntent.OnSearchClicked(query)) },
        onPlaceClick = { place ->
            viewModel.onIntent(KakaoMapIntent.OnPlaceItemClicked(place))
        },
        onBackClick = handleBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KakaoMapSearchScreen(
    modifier: Modifier = Modifier,
    searchQuery: String,
    isLoading: Boolean,
    searchResults: List<KakaoMapPresentationModel>,
    autoCompleteResults: List<KakaoMapPresentationModel>,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    onPlaceClick: (KakaoMapPresentationModel) -> Unit,
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100.milliseconds)
        focusRequester.requestFocus()
    }

    BackHandler {
        onBackClick()
    }

    Column(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. 상단 검색바 영역 (아이콘 위치를 일정 편집 화면과 일치시킴)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(com.braveberry.tourdataproject.R.drawable.arrow_circle_left),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("장소를 검색하세요", fontSize = 15.sp) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    cursorColor = PrimaryTeal
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch(searchQuery)
                        focusManager.clearFocus()
                    }
                )
            )

            Spacer(modifier = Modifier.width(12.dp))
        }

        HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryTeal)
            } else {
                val displayResults = if (autoCompleteResults.isNotEmpty()) autoCompleteResults else searchResults
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(displayResults, key = { it.id }) { place ->
                        PlaceItem(
                            place = place,
                            onClick = {
                                onPlaceClick(place)
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceItem(place: KakaoMapPresentationModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = place.placeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = place.address, color = Color.Gray, fontSize = 14.sp)
    }
    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun KakaoMapSearchScreenPreview() {
    KakaoMapSearchScreen(
        searchQuery = "서울역",
        isLoading = false,
        searchResults = listOf(
            KakaoMapPresentationModel(
                id = "1",
                placeName = "서울역",
                address = "서울 용산구 한강대로 405",
                distanceText = "1.2km",
                x = 126.9723,
                y = 37.5546,
                category = "교통",
                phone = "02-1234-5678"
            )
        ),
        autoCompleteResults = emptyList(),
        onQueryChanged = {},
        onSearch = {},
        onPlaceClick = { _ -> },
        onBackClick = {}
    )
}
