package com.braveberry.tourdataproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
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
import com.braveberry.tourdataproject.screen.plan.ListRoute
import com.braveberry.tourdataproject.screen.plan.MakeCourseRoute
import com.braveberry.tourdataproject.screen.plan.RegionSelectionRoute
import com.braveberry.tourdataproject.screen.plan.ScheduleEditRoute
import com.braveberry.tourdataproject.screen.splash.SplashScreen
import com.braveberry.tourdataproject.ui.theme.TourDataProjectTheme
import com.tourdataproject.presentation.viewmodel.plan.PlanSharedIntent
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
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(
                            onInitComplete = {
                                // 초기화 완료 시 메인 화면(course_list)으로 이동
                                navController.navigate("course_list") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("course_list") {
                        ListRoute(
                            onNavigateToCreateNewCourse = {
                                // 새 플랜 생성 시 plan_graph의 startDestination인 region_selection으로 진입
                                navController.navigate("plan_graph")
                            },
                            onNavigateToCourseDetail = { courseId ->
                                // 기존 플랜 클릭 시 courseId를 담아 make_course로 직접 진입
                                navController.navigate("make_course?courseId=$courseId")
                            }
                        )
                    }

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
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("date_selection") { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            DateSelectionRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateToNext = { navController.navigate("make_course") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // courseId를 선택적 인자로 받도록 라우트 수정
                        composable(
                            route = "make_course?courseId={courseId}",
                            arguments = listOf(
                                navArgument("courseId") {
                                    type = NavType.StringType
                                    nullable = true
                                }
                            )
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            MakeCourseRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddSchedule = {
                                    navController.navigate("add_location")
                                },
                                onNavigateToEditSchedule = { dayNum ->
                                    navController.navigate("editSchedule/$dayNum")
                                },
                                onShowToast = { message ->
                                    // 토스트 처리
                                },
                                onNavigateToHome = {
                                    // 홈으로 돌아갈 때는 백스택을 정리하며 course_list로 이동
                                    navController.popBackStack("course_list", inclusive = false)
                                }
                            )
                        }

                        composable(
                            route = "editSchedule/{dayNum}",
                            arguments = listOf(navArgument("dayNum") { type = NavType.IntType })
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            ScheduleEditRoute(
                                sharedViewModel = sharedViewModel,
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
                                    // make_course로 돌아갈 때 인자 없이 라우트 이름만 사용해도 매칭됩니다
                                    navController.popBackStack(route = "make_course?courseId={courseId}", inclusive = false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
