package com.braveberry.tourdataproject.screen.plan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.viewmodel.course.addSchedule.AddScheduleDetailViewModel
import com.tourdataproject.presentation.viewmodel.course.addSchedule.uiState.AddScheduleDetailEffect
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedEvent // 🌟 이벤트 임포트 추가
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel


data class AddScheduleInitModel(
    val placeName: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isValid: Boolean = false
)

fun ScheduleItemUiModel?.toInitModel(): AddScheduleInitModel {
    return if (this == null) {
        AddScheduleInitModel(isValid = false)
    } else {
        AddScheduleInitModel(
            placeName = this.scheduleName,
            address = this.address ?: "",
            latitude = this.latitude,
            longitude = this.longitude,
            isValid = true
        )
    }
}

@Composable
fun AddScheduleDetailRoute(
    sharedViewModel: PlanSharedViewModel,
    viewModel: AddScheduleDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCourse: () -> Unit
) {

    val draftSchedule by sharedViewModel.draftSchedule.collectAsState()
    val initModel = draftSchedule.toInitModel()

    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(initModel) {
        if (initModel.isValid) {
            if (uiState.placeName.isBlank()) {
                viewModel.setInitialPlace(
                    name = initModel.placeName,
                    address = initModel.address,
                    lat = initModel.latitude,
                    lng = initModel.longitude
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddScheduleDetailEffect.SubmitSchedule -> {
                    sharedViewModel.setEvent(
                        PlanSharedEvent.OnConfirmAndAddSchedule(
                            memoInput = effect.memo,
                            accessibilityInfo = effect.accessibilityInfo
                        )
                    )
                    onNavigateToCourse()
                }
                is AddScheduleDetailEffect.NavigateBack -> {
                    sharedViewModel.setEvent(PlanSharedEvent.OnClearDraftSchedule)
                    onNavigateBack()
                }
            }
        }
    }

    BackHandler {
        viewModel.onBackClicked()
    }

    if (uiState.isValid) {
        AddScheduleDetailScreen(
            placeName = uiState.placeName,
            address = uiState.address,
            memo = uiState.memo,
            onMemoChange = viewModel::onMemoChanged,
            onBackClick = viewModel::onBackClicked,
            onSaveClick = viewModel::onSaveClicked
        )
    }
}

@Composable
fun AddScheduleDetailScreen(
    placeName: String,
    address: String,
    memo: String,
    onMemoChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "일정 정보 추가",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            }
        },
        bottomBar = {
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1BA68D)
                )
            ) {
                Text(text = "저장", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = placeName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "이름 수정",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (address.isNotBlank()) {
                Text(
                    text = address,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "메모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = memo,
                onValueChange = onMemoChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text(
                        text = "이 장소에서 참고할 내용을 적어주세요.",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF1BA68D),
                    cursorColor = Color(0xFF1BA68D)
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AddScheduleDetailScreenPreview() {
    AddScheduleDetailScreen(
        placeName = "학동흑진주몽돌해변",
        address = "경남 거제시 동부면 학동리",
        memo = "",
        onMemoChange = {},
        onBackClick = {},
        onSaveClick = {}
    )
}