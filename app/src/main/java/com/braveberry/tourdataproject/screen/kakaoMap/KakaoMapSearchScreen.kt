package com.braveberry.tourdataproject.screen.kakaoMap

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tourdataproject.presentation.KakaoMapEffect
import com.tourdataproject.presentation.KakaoMapEvent
import com.tourdataproject.presentation.model.KakaoMapUiModel
import com.tourdataproject.presentation.viewmodel.kakaoMap.KakaoMapViewModel
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedEvent // 🌟 이벤트 임포트
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun KakaoMapSearchRoute(
    sharedViewModel: PlanSharedViewModel,
    modifier: Modifier = Modifier,
    viewModel: KakaoMapViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToNext: () -> Unit
) {
    val uiState by viewModel.container.stateFlow.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(KakaoMapEvent.OnSearchQueryChanged(""))

        // 🌟 SharedViewModel에서 저장해둔 목적지 좌표를 꺼내서 카카오맵 뷰모델로 주입!
        val courseState = sharedViewModel.courseState.value
        val lat = courseState.destinationLatitude
        val lng = courseState.destinationLongitude

        // 🌟 좌표가 정상적으로 있다면 카카오맵 뷰모델 초기화 이벤트 발송
        if (lat != 0.0 && lng != 0.0) {
            viewModel.onEvent(KakaoMapEvent.OnInitLocation(lat, lng))
        } else {
            // (선택 사항) 만약 좌표가 0.0이면 에러 처리 로직 추가 가능
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is KakaoMapEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is KakaoMapEffect.NavigateNextScreen -> {
                    sharedViewModel.setEvent(PlanSharedEvent.OnSetDraftSchedule(effect.place))
                    onNavigateToNext()
                }
            }
        }
    }

    val handleBackClick = {
        sharedViewModel.setEvent(PlanSharedEvent.OnClearDraftSchedule)
        onBackClick()
    }

    KakaoMapSearchScreen(
        modifier = modifier,
        searchQuery = uiState.searchQuery,
        isLoading = uiState.isLoading,
        searchResults = uiState.searchResults,
        autoCompleteResults = uiState.autoCompleteResults,
        onQueryChanged = { viewModel.onEvent(KakaoMapEvent.OnSearchQueryChanged(it)) },
        onSearch = { query -> viewModel.onEvent(KakaoMapEvent.OnSearchClicked(query)) },
        onPlaceClick = { place -> viewModel.onEvent(KakaoMapEvent.OnPlaceItemClicked(place)) },
        onBackClick = handleBackClick
    )
}

@Composable
fun KakaoMapSearchScreen(
    modifier: Modifier = Modifier,
    searchQuery: String,
    isLoading: Boolean,
    searchResults: List<KakaoMapUiModel>,
    autoCompleteResults: List<KakaoMapUiModel>,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    onPlaceClick: (KakaoMapUiModel) -> Unit,
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // 화면이 완전히 그려지기 전에 포커스를 요청하면 무시될 수 있어 아주 짧은 딜레이를 줍니다.
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
        // 1. 상단 검색바 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("장소를 검색하세요") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch(searchQuery)
                        focusManager.clearFocus()
                    }
                )
            )
            Button(
                onClick = {
                    onSearch(searchQuery)
                    focusManager.clearFocus()
                },
                modifier = Modifier.height(56.dp)
            ) {
                Text("검색")
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (autoCompleteResults.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(autoCompleteResults, key = { it.id }) { place ->
                        PlaceItem(
                            place = place,
                            onClick = {
                                onPlaceClick(place)
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults, key = { it.id }) { place ->
                        PlaceItem(
                            place = place,
                            onClick = {
                                onPlaceClick(place)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceItem(place: KakaoMapUiModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = place.placeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = place.address, color = Color.Gray, fontSize = 14.sp)

        if (place.distanceText.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = place.distanceText, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
    }
    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun KakaoMapSearchScreenPreview() {
    KakaoMapSearchScreen(
        searchQuery = "서울역",
        isLoading = false,
        searchResults = listOf(
            KakaoMapUiModel(
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