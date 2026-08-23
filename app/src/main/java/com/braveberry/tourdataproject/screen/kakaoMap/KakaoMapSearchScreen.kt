package com.braveberry.tourdataproject.screen.kakaoMap

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tourdataproject.presentation.KakaoMapSideEffect
import com.tourdataproject.presentation.model.KakaoMapUiModel
import com.tourdataproject.presentation.viewmodel.KakaoMapViewModel

@Composable
fun KakaoMapSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: KakaoMapViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onPlaceSelected: (Double, Double) -> Unit
) {
    val uiState by viewModel.container.stateFlow.collectAsState()

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    BackHandler {
        onBackClick()
    }
    LaunchedEffect(Unit) {
        viewModel.updateSearchQuery("")
    }
    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is KakaoMapSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is KakaoMapSideEffect.NavigateBackToMap -> {
                    onPlaceSelected(effect.x, effect.y)
                }
            }
        }
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
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
            }

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = {
                    viewModel.updateSearchQuery(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                placeholder = { Text("장소를 검색하세요") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.searchPlaces(query = uiState.searchQuery)
                        focusManager.clearFocus()
                    }
                )
            )
        }

        Divider()

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                // TODO: 로딩시 화면 추후 수정필요한지?
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.autoCompleteResults.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.autoCompleteResults, key = { it.id }) { place ->
                        PlaceItem(
                            place = place,
                            onClick = {
                                viewModel.selectPlace(place.x, place.y)
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            } else {
                // 🌟 검색 완료 후 돋보기 버튼을 눌렀을 때 나오는 진짜 결과창
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.searchResults, key = { it.id }) { place ->
                        PlaceItem(
                            place = place,
                            onClick = {
                                viewModel.selectPlace(place.x, place.y)
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
