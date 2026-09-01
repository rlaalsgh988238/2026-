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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel

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
    }

    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is KakaoMapEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is KakaoMapEffect.NavigateNextScreen -> {
                    sharedViewModel.setDraftSchedule(effect.place)
                    onNavigateToNext()
                }
            }
        }
    }

    val handleBackClick = {
        sharedViewModel.clearDraftSchedule()
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
    onPlaceClick: (KakaoMapUiModel) -> Unit, // 🌟 파라미터 타입 변경
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    BackHandler {
        onBackClick()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    .padding(end = 8.dp),
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
    Divider(color = Color.LightGray, thickness = 0.5.dp)
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
        onPlaceClick = { _ -> }, // 🌟 람다 파라미터 수정
        onBackClick = {}
    )
}