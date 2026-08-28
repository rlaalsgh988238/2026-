package com.braveberry.tourdataproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.braveberry.tourdataproject.screen.plan.DateSelectionRoute
import com.braveberry.tourdataproject.screen.plan.RegionSelectionRoute
import com.braveberry.tourdataproject.ui.theme.TourDataProjectTheme
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TourDataProjectTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "plan_graph") {
                    // 여행 계획 중첩 그래프
                    navigation(startDestination = "region_selection", route = "plan_graph") {

                        // [화면 1] 지역 선택
                        composable("region_selection") { entry ->
                            // plan_graph 스코프의 공유 뷰모델 가져오기
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            RegionSelectionRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateToDateSelection = {
                                    navController.navigate("date_selection")
                                },
                                onNavigateBack = { finish() }
                            )
                        }

                        // [화면 2] 날짜 선택
                        composable("date_selection") { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            DateSelectionRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateToNext = { /* 다음 단계로 */ },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

