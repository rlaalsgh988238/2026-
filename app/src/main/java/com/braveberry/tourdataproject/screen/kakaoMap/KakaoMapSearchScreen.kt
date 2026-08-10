package com.braveberry.tourdataproject.screen.kakaoMap

import android.widget.Toast
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
import com.tourdataproject.map_presentation.KakaoMapSideEffect
import com.tourdataproject.map_presentation.KakaoMapViewModel
import com.tourdataproject.map_presentation.model.KakaoMapUiModel

@Composable
fun KakaoMapSearchScreen(
    modifier: Modifier = Modifier,
    viewModel: KakaoMapViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    // 검색된 장소를 클릭했을 때 좌표를 메인 화면으로 돌려주기 위한 콜백
    onPlaceSelected: (Double, Double) -> Unit
) {
    // 🌟 Orbit MVI State 관찰
    val uiState by viewModel.container.stateFlow.collectAsState()

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // 🌟 Orbit MVI SideEffect(토스트 등) 관찰
    LaunchedEffect(viewModel) {
        viewModel.container.sideEffectFlow.collect { effect ->
            when (effect) {
                is KakaoMapSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                else -> {
                    //TODO: SideEffect에 또 다른거 넣을 때 사용
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
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                placeholder = { Text("장소를 검색하세요") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        // 🌟 여기서 통신 시작! (임시로 전국 검색을 위해 x, y는 null)
                        viewModel.searchPlaces(query = uiState.searchQuery)
                        focusManager.clearFocus()
                    }
                )
            )
        }

        Divider()

        // 2. 검색 결과 리스트 & 로딩 인디케이터
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.searchResults, key = { it.id }) { place ->
                        PlaceItem(
                            place = place,
                            onClick = {
                                // 🌟 장소를 클릭하면 해당 x, y 좌표를 들고 이전 화면(지도)으로 복귀!
                                onPlaceSelected(place.x, place.y)
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

        // 거리가 존재할 경우에만 표시 (전국 검색 시 거리가 없을 수도 있음)
        if (place.distanceText.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = place.distanceText, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
    }
    Divider(color = Color.LightGray, thickness = 0.5.dp)
}