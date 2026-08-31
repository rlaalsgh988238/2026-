package com.braveberry.tourdataproject.screen.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.braveberry.tourdataproject.ui.theme.SearchPlaceGray

@Composable
fun AddLocationRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit, // 검색창 클릭 시 실제 검색 화면으로 이동
) {
    AddLocationScreen(
        onBackClick = onNavigateBack,
        onSearchClick = onNavigateToSearch
    )
}

// Screen 컴포저블 (오직 기획안 UI 디자인에만 집중)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Scaffold(
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
                    // 뒤로가기 버튼 (좌측)
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

                    // 타이틀 (가운데)
                    Text(
                        text = "장소 추가",
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
                text = "어디를 방문할건가요?",
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
        onBackClick = {},
        onSearchClick = {}
    )
}