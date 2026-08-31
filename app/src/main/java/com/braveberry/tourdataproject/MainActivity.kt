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
import com.braveberry.tourdataproject.screen.kakaoMap.KakaoMapSearchRoute
import com.braveberry.tourdataproject.screen.plan.AddLocationRoute
import com.braveberry.tourdataproject.screen.plan.AddScheduleDetailRoute
import com.braveberry.tourdataproject.screen.plan.DateSelectionRoute
import com.braveberry.tourdataproject.screen.plan.MakeCourseRoute
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
                            android.util.Log.d("NavDebug", "🚨 엥? 진짜 날짜 선택 화면으로 강제 이동됨!")

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
                                    android.util.Log.d("NavDebug", "🚨 MakeCourseRoute에서 뒤로가기 명령 발동됨!")
                                                 },
                                onNavigateToAddSchedule = {
                                    navController.navigate("add_location")
                                },
                                onShowToast = { message ->
                                    // 토스트 처리
                                }
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
                                    navController.navigate("add_schedule_detail") // (이동할 목적지 라우트 이름에 맞게 수정)
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

