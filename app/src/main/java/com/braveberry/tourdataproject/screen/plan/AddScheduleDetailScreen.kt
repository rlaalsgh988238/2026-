package com.braveberry.tourdataproject.screen.plan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tourdataproject.presentation.model.course.ScheduleItemUiModel
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel

data class AddScheduleDetailUiState(
    val isValid: Boolean = false, // 임시 데이터가 정상적으로 존재하는지 확인
    val placeName: String = "",
    val address: String = ""
)

// ====================================================================
// 2. Mapper (ScheduleItemUiModel? -> AddScheduleDetailUiState)
// ====================================================================
fun ScheduleItemUiModel?.toAddScheduleDetailState(): AddScheduleDetailUiState {
    return if (this == null) {
        AddScheduleDetailUiState(isValid = false)
    } else {
        AddScheduleDetailUiState(
            isValid = true,
            placeName = this.scheduleName,
            address = this.address ?: ""
        )
    }
}
@Composable
fun AddScheduleDetailRoute(
    sharedViewModel: PlanSharedViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCourse: () -> Unit
) {
    val draftSchedule by sharedViewModel.draftSchedule.collectAsState()
    val uiState = draftSchedule.toAddScheduleDetailState()

    var memo by rememberSaveable { mutableStateOf("") }

    // 🌟 1. '저장 버튼을 눌러서 나가는 중인지'를 기억할 상태 변수 추가
    var isSaving by rememberSaveable { mutableStateOf(false) }

    // 만약 예기치 않게 임시 데이터가 없다면 (isValid == false) 뒤로 돌려보냄
    LaunchedEffect(uiState.isValid) {
        // 🌟 2. 저장 중(isSaving)이 아닐 때만 튕겨내도록 방어!
        if (!uiState.isValid && !isSaving) {
            onNavigateBack()
        }
    }

    val handleBackClick = {
        sharedViewModel.clearDraftSchedule()
        onNavigateBack()
    }

    BackHandler {
        handleBackClick()
    }

    if (uiState.isValid) {
        AddScheduleDetailScreen(
            placeName = uiState.placeName,
            address = uiState.address,
            memo = memo,
            onMemoChange = { memo = it },
            onBackClick = handleBackClick,
            onSaveClick = {
                // 🌟 3. 저장 버튼을 누르는 순간 깃발을 꽂아서 LaunchedEffect의 암살을 막음
                isSaving = true

                sharedViewModel.confirmAndAddSchedule(memo)
                onNavigateToCourse()
            }
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
                Divider(color = Color.LightGray, thickness = 0.5.dp)
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
                    containerColor = Color(0xFF1BA68D) // 이미지와 유사한 청록색
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
            // 1. 장소 이름 및 편집 아이콘
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

            // 2. 주소
            if (address.isNotBlank()) {
                Text(
                    text = address,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. 메모 타이틀
            Text(
                text = "메모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. 메모 입력창
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
        address = "거제시 동부면",
        memo = "",
        onMemoChange = {},
        onBackClick = {},
        onSaveClick = {}
    )
}
