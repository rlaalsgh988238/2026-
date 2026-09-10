package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.ui.theme.SearchPlaceGray
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedState
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.plan.addLocation.AddLocationViewModel
import com.tourdataproject.presentation.viewmodel.plan.addLocation.uiState.AddLocationState
import com.tourdataproject.presentation.viewmodel.plan.addLocation.uiState.AddLocationViewMode

//TODO 여기 튕겼다가 다시 들어오면 검색 안됨
@Composable
fun AddLocationRoute(
    viewModel: AddLocationViewModel = hiltViewModel(),
    sharedViewModel: PlanSharedViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSearch: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharedState by sharedViewModel.sharedState.collectAsStateWithLifecycle()

    AddLocationScreen(
        state = state,
        sharedState = sharedState,
        onBackClick = onNavigateBack,
        onSearchClick = { onNavigateToSearch(state.purpose ?: "unknown") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    state: AddLocationState,
    sharedState: PlanSharedState,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = Color.White,
        topBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color.White)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "뒤로 가기",
                                tint = Color.Black
                            )
                        }
                    }

                    Text(
                        text = state.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = when(state.viewMode){
                    AddLocationViewMode.AddScheduleMode -> "어디를 방문할껀가요?"
                    AddLocationViewMode.AddStayMode -> sharedState.course.destination
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = onSearchClick,
                shape = RoundedCornerShape(12.dp),
                color = SearchPlaceGray,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색 아이콘",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "장소명을 입력해주세요",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "내 여행 도시를 기반으로 검색되며, 카카오맵에 등록된 명칭으로 입력 시 더 정확하게 찾을 수 있어요.",
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AddLocationScreenPreview() {
    AddLocationScreen(
        state = AddLocationState(),
        sharedState = PlanSharedState(),
        onBackClick = {},
        onSearchClick = {}
    )
}