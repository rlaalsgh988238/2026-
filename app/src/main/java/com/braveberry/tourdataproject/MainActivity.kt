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
import com.braveberry.tourdataproject.screen.kakaoMap.KakaoMapSearchRoute
import com.braveberry.tourdataproject.screen.plan.AddLocationRoute
import com.braveberry.tourdataproject.screen.plan.AddScheduleDetailRoute
import com.braveberry.tourdataproject.screen.plan.DateSelectionRoute
import com.braveberry.tourdataproject.screen.main.ListRoute
import com.braveberry.tourdataproject.screen.plan.FullCourseMapRoute
import com.braveberry.tourdataproject.screen.plan.MakeCourseRoute
import com.braveberry.tourdataproject.screen.plan.RegionSelectionRoute
import com.braveberry.tourdataproject.screen.plan.ScheduleEditRoute
import com.braveberry.tourdataproject.screen.splash.SplashScreen
import com.braveberry.tourdataproject.screen.toilet.NearbyToiletListRoute
import com.braveberry.tourdataproject.screen.toilet.NearbyToiletListScreen
import com.braveberry.tourdataproject.ui.theme.TourDataProjectTheme
import com.tourdataproject.presentation.utility.ScreenPurpose
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

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(
                            onInitComplete = {
                                navController.navigate("course_list") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("course_list") {
                        ListRoute(
                            onNavigateToCreateNewCourse = {
                                navController.navigate("plan_graph")
                            },
                            onNavigateToCourseDetail = { courseId ->
                                navController.navigate("make_course?courseId=$courseId&purpose=VIEW_EXISTING_COURSE")
                            },
                            onNavigateToNearbyToilet = {
                                navController.navigate("nearby_toilet")
                            }

                        )
                    }

                    composable(route = "nearby_toilet") {
                        NearbyToiletListRoute(
                            onBackClick = { navController.popBackStack() }
                        )
                    }


                    navigation(
                        startDestination = "region_selection?from={from}&purpose={purpose}",
                        route = "plan_graph"
                    ) {

                        // 1. region_selection
                        composable(
                            route = "region_selection?from={from}&purpose={purpose}",
                            arguments = listOf(
                                navArgument("from") { defaultValue = "UNKNOWN" },
                                navArgument("purpose") { defaultValue = "CREATE_NEW_COURSE" }
                            )
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )
                            val purpose = entry.arguments?.getString("purpose") ?: "CREATE_NEW_COURSE"

                            RegionSelectionRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateToDateSelection = {
                                    navController.navigate("date_selection?from=region_selection&purpose=$purpose")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 2. date_selection
                        composable(
                            route = "date_selection?from={from}&purpose={purpose}",
                            arguments = listOf(
                                navArgument("from") { defaultValue = "UNKNOWN" },
                                navArgument("purpose") { defaultValue = "UNKNOWN" }
                            )
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )
                            val purpose = entry.arguments?.getString("purpose") ?: "UNKNOWN"

                            DateSelectionRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateToNext = {
                                    if (purpose == ScreenPurpose.ADD_STAY) {
                                        // 숙소 체크인-체크아웃 확정 후에는 상세화면 없이 바로 코스 화면으로 복귀
                                        navController.popBackStack(
                                            route = "make_course?courseId={courseId}&from={from}&purpose={purpose}",
                                            inclusive = false
                                        )
                                    } else {
                                        navController.navigate("make_course?from=date_selection&purpose=$purpose")
                                    }
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 3. make_course
                        composable(
                            route = "make_course?courseId={courseId}&from={from}&purpose={purpose}",
                            arguments = listOf(
                                navArgument("courseId") { type = NavType.StringType; nullable = true },
                                navArgument("from") { defaultValue = "UNKNOWN" },
                                navArgument("purpose") { defaultValue = "UNKNOWN" }
                            )
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            MakeCourseRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAddSchedule = { schedulePurpose ->
                                    navController.navigate("add_location?from=make_course&purpose=$schedulePurpose")
                                },
                                onNavigateToEditSchedule = { dayNum, purpose ->
                                    navController.navigate("editSchedule/$dayNum?from=make_course&purpose=$purpose")
                                },
                                onNavigateToAddStay = { purpose ->
                                    navController.navigate("add_location?from=make_course&purpose=$purpose")
                                },
                                onNavigateToHome = {
                                    navController.popBackStack("course_list", inclusive = false)
                                },
                                onNavigateToFullMap = {
                                    navController.navigate("full_course_map")
                                }
                            )
                        }

                        // 4. editSchedule
                        composable(
                            route = "editSchedule/{dayNum}?from={from}&purpose={purpose}",
                            arguments = listOf(
                                navArgument("dayNum") { type = NavType.IntType },
                                navArgument("from") { defaultValue = "UNKNOWN" },
                                navArgument("purpose") { defaultValue = "UNKNOWN" }
                            )
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            ScheduleEditRoute(
                                sharedViewModel = sharedViewModel,
                                viewModel = hiltViewModel(),
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // 5. add_location
                        composable(
                            route = "add_location?from={from}&purpose={purpose}",
                            arguments = listOf(
                                navArgument("from") { defaultValue = "UNKNOWN" },
                                navArgument("purpose") { defaultValue = "UNKNOWN" }
                            )
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )
                            val purpose = entry.arguments?.getString("purpose") ?: "UNKNOWN"

                            AddLocationRoute(
                                viewModel = hiltViewModel(),
                                sharedViewModel = sharedViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToSearch = {
                                    navController.navigate("kakao_map_search?from=add_location&purpose=$purpose")
                                }
                            )
                        }

                        // 6. kakao_map_search
                        composable(
                            route = "kakao_map_search?from={from}&purpose={purpose}",
                            arguments = listOf(
                                navArgument("from") { defaultValue = "UNKNOWN" },
                                navArgument("purpose") { defaultValue = "UNKNOWN" }
                            )
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )
                            val purpose = entry.arguments?.getString("purpose") ?: "UNKNOWN"

                            KakaoMapSearchRoute(
                                sharedViewModel = sharedViewModel,
                                onBackClick = { navController.popBackStack() },
                                onNavigateToNext = {
                                    navController.navigate("add_schedule_detail?from=kakao_map_search&purpose=$purpose")
                                },
                                onNavigateToDateSelect = { stayPurpose ->
                                    navController.navigate("date_selection?from=kakao_map_search&purpose=$stayPurpose")
                                }
                            )
                        }

                        // 7. add_schedule_detail
                        composable(
                            route = "add_schedule_detail?from={from}&purpose={purpose}",
                            arguments = listOf(
                                navArgument("from") { defaultValue = "UNKNOWN" },
                                navArgument("purpose") { defaultValue = "UNKNOWN" }
                            )
                        ) { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            AddScheduleDetailRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToCourse = {
                                    navController.popBackStack(
                                        route = "make_course?courseId={courseId}&from={from}&purpose={purpose}",
                                        inclusive = false
                                    )
                                }
                            )
                        }

                        // 8. full_course_map
                        composable(route = "full_course_map") { entry ->
                            val sharedViewModel: PlanSharedViewModel = hiltViewModel(
                                remember(entry) { navController.getBackStackEntry("plan_graph") }
                            )

                            FullCourseMapRoute(
                                sharedViewModel = sharedViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                    }
                }
            }
        }
    }
}
