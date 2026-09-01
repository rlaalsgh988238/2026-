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
import com.braveberry.tourdataproject.screen.splash.SplashScreen
import com.braveberry.tourdataproject.ui.theme.TourDataProjectTheme
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedViewModel
import com.tourdataproject.presentation.viewmodel.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TourDataProjectTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash" // 시작점을 스플래시로 변경
                ) {
                    composable("splash") {
                        val splashViewModel: SplashViewModel = hiltViewModel()
                        SplashScreen(
                            viewModel = splashViewModel,
                            onInitComplete = {
                                // 초기화 완료 시 메인 그래프로 이동 (백스택 제거)
                                navController.navigate("plan_graph") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 여행 계획 중첩 그래프
                    navigation(startDestination = "region_selection", route = "plan_graph") {
                        composable("region_selection") { entry ->
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
