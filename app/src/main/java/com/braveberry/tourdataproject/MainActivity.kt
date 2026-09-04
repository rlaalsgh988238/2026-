package com.braveberry.tourdataproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.braveberry.tourdataproject.screen.MockStartScreen
import com.braveberry.tourdataproject.screen.kakaoMap.KakaoMapSearchRoute
import com.braveberry.tourdataproject.screen.plan.AddLocationRoute
import com.braveberry.tourdataproject.screen.plan.AddScheduleDetailRoute
import com.braveberry.tourdataproject.screen.plan.DateSelectionRoute
import com.braveberry.tourdataproject.screen.plan.MakeCourseRoute
import com.braveberry.tourdataproject.screen.plan.RegionSelectionRoute
import com.braveberry.tourdataproject.screen.plan.ScheduleEditRoute
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

                        composable("mock_start") {entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )
                            MockStartScreen(
                                sharedViewModel = sharedViewModel,
                                onNavigateToMakeCourse = {
                                    navController.navigate("make_course")
                                }
                            )
                        }
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
                                onNavigateToNext = {navController.navigate("make_course") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }


                        composable("make_course") { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            MakeCourseRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateBack = { navController.popBackStack()
                                                 },
                                onNavigateToAddSchedule = {
                                    navController.navigate("add_location")
                                },
                                onNavigateToEditSchedule = { dayNum -> // Int 인자를 받도록 수정
                                    navController.navigate("editSchedule/$dayNum")
                                },
                                onShowToast = { message ->
                                    // 토스트 처리
                                }
                            )
                        }

                        composable(
                            route = "editSchedule/{dayNum}",
                            arguments = listOf(navArgument("dayNum") { type = NavType.IntType })
                        ) { entry ->
                            // 부모 그래프의 공유 뷰모델 가져오기
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            // ScheduleEditRoute 호출
                            ScheduleEditRoute(
                                sharedViewModel = sharedViewModel,
                                // 여기서 hiltViewModel()이 내부적으로 SavedStateHandle을 통해 dayNum을 가져갑니다.
                                viewModel = hiltViewModel(),
                                onNavigateBack = { navController.popBackStack() },
                                onShowToast = { /* 토스트 처리 */ }
                            )
                        }

                        composable("add_location") {
                            AddLocationRoute(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToSearch = {
                                    navController.navigate("kakao_map_search")
                                }
                            )
                        }
                        composable("kakao_map_search") { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            KakaoMapSearchRoute(
                                sharedViewModel = sharedViewModel,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToNext = {
                                    navController.navigate("add_schedule_detail")
                                }
                            )
                        }
                        composable("add_schedule_detail") { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            AddScheduleDetailRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToCourse = {

                                    val currentBackStack = navController.backQueue.map { it.destination.route }
                                    android.util.Log.d("NavDebug", "2. popBackStack 직전 백스택: $currentBackStack")
                                    navController.popBackStack(route = "make_course", inclusive = false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
