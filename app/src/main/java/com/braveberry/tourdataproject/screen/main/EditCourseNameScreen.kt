package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.braveberry.tourdataproject.R
import com.braveberry.tourdataproject.ui.theme.Mint100
import com.tourdataproject.presentation.viewmodel.course.editCourseName.EditCourseNameViewModel
import com.tourdataproject.presentation.viewmodel.course.editCourseName.uiState.EditCourseNameEffect
import com.tourdataproject.presentation.viewmodel.course.editCourseName.uiState.EditCourseNameIntent


@Composable
fun EditCourseNameRoute(
    courseId: String,
    initialName: String,
    viewModel: EditCourseNameViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is EditCourseNameEffect.NavigateBack -> onNavigateUp()
                is EditCourseNameEffect.ShowToast -> onShowToast(effect.message)
            }
        }
    }

    EditCourseNameScreen(
        initialName = initialName,
        onBackClick = { viewModel.onIntent(EditCourseNameIntent.OnBackClicked) },
        onSaveClick = { newName ->
            viewModel.onIntent(EditCourseNameIntent.OnSaveClicked(courseId, newName))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCourseNameScreen(
    initialName: String,
    onBackClick: () -> Unit,
    onSaveClick: (String) -> Unit
) {
    var courseName by remember { mutableStateOf(initialName) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = Color.White,
        topBar = {
            Column {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color.White)
                        .padding(start = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_circle_left),
                            contentDescription = "뒤로가기",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.Unspecified
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "여행 이름 변경",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(52.dp))
                }
                HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            }
        },
        bottomBar = {
            // 하단 고정 버튼 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { onSaveClick(courseName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "저장",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        // 중앙 입력 콘텐츠 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold가 계산한 TopBar, BottomBar 높이만큼 패딩 적용
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = "여행 이름을 입력해주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Mint100,
                    unfocusedBorderColor = Color(0xFFD9D9D9),
                    cursorColor = Mint100
                ),
                singleLine = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditCourseNameScreenPreview() {
    EditCourseNameScreen(
        initialName = "제주도 여행",
        onBackClick = {},
        onSaveClick = {}
    )
}