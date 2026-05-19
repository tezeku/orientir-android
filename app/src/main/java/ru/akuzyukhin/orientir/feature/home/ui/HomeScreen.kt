package ru.akuzyukhin.orientir.feature.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.akuzyukhin.orientir.core.ui.CollectAsEffect
import ru.akuzyukhin.orientir.feature.auth.domain.model.Role
import ru.akuzyukhin.orientir.feature.connections.ui.add_ward.AddWardScreen
import ru.akuzyukhin.orientir.feature.connections.ui.connections.ConnectionsScreen
import ru.akuzyukhin.orientir.feature.notification.ui.list.NotificationsScreen
import ru.akuzyukhin.orientir.feature.profile.ui.edit.ProfileEditScreen
import ru.akuzyukhin.orientir.feature.profile.ui.password.ChangePasswordScreen
import ru.akuzyukhin.orientir.feature.profile.ui.profile.ProfileScreen
import ru.akuzyukhin.orientir.feature.schedule.ui.detail.ScheduleDetailScreen
import ru.akuzyukhin.orientir.feature.schedule.ui.list.SchedulesListScreen
import ru.akuzyukhin.orientir.feature.statistics.ui.statistics.WardStatisticsScreen
import ru.akuzyukhin.orientir.feature.statistics.ui.thresholds.WardThresholdsScreen
import ru.akuzyukhin.orientir.feature.task.ui.daily.ScheduleTabRouterScreen
import ru.akuzyukhin.orientir.feature.task.ui.daily.curator.CuratorWardDailyScreen
import ru.akuzyukhin.orientir.feature.task.ui.editor.TaskEditorScreen
import ru.akuzyukhin.orientir.feature.task.ui.statistics.StatisticsScreen
import ru.akuzyukhin.orientir.navigation.HomeTabRoutes
import ru.akuzyukhin.orientir.ui.theme.OrientirTheme
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    val homeNavController = rememberNavController()

    Scaffold(
        bottomBar = { HomeBottomBar(navController = homeNavController) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)

    ) { padding ->
        NavHost(
            navController = homeNavController,
            startDestination = HomeTabRoutes.SCHEDULE,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(HomeTabRoutes.PROFILE) {
                ProfileScreen(
                    onNavigateToEdit = {
                        homeNavController.navigate(HomeTabRoutes.PROFILE_EDIT)
                    },
                    onNavigateToChangePassword = {
                        homeNavController.navigate(HomeTabRoutes.CHANGE_PASSWORD)
                    },
                    onNavigateToConnections = {
                        homeNavController.navigate(HomeTabRoutes.CONNECTIONS)
                    },
                    onNavigateToLogin = onLogout
                )
            }
            composable(HomeTabRoutes.PROFILE_EDIT) {
                ProfileEditScreen(
                    onNavigateBack = { homeNavController.popBackStack() }
                )
            }
            composable(HomeTabRoutes.CHANGE_PASSWORD) {
                ChangePasswordScreen(
                    onNavigateBack = { homeNavController.popBackStack() }
                )
            }

            composable(HomeTabRoutes.CONNECTIONS) {
                ConnectionsScreen(
                    onNavigateBack = { homeNavController.popBackStack() },
                    onNavigateToAddWard = {
                        homeNavController.navigate(HomeTabRoutes.ADD_WARD)
                    },
                    onNavigateToWardSchedules = { wardId ->
                        homeNavController.navigate(HomeTabRoutes.wardSchedules(wardId))
                    }
                )
            }

            composable(
                route = HomeTabRoutes.WARD_SCHEDULES_ROUTE,
                arguments = listOf(
                    navArgument(HomeTabRoutes.WARD_ID_ARG) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val wardId = backStackEntry.arguments?.getLong(HomeTabRoutes.WARD_ID_ARG) ?: return@composable
                SchedulesListScreen(
                    onNavigateBack = { homeNavController.popBackStack() },
                    onNavigateToScheduleDetail = { scheduleId ->
                        homeNavController.navigate(HomeTabRoutes.scheduleDetail(wardId, scheduleId))
                    },
                    onNavigateToDaily = {
                        homeNavController.navigate(HomeTabRoutes.wardDaily(wardId))
                    },
                    onNavigateToStatistics = { id ->
                        homeNavController.navigate(HomeTabRoutes.wardStatistics(id))
                    },
                    onNavigateToThresholds = { id ->
                        homeNavController.navigate(HomeTabRoutes.wardThresholds(id))
                    }
                )
            }

            composable(
                route = HomeTabRoutes.WARD_DAILY_ROUTE,
                arguments = listOf(
                    navArgument(HomeTabRoutes.WARD_ID_ARG) { type = NavType.LongType }
                )
            ) {
                CuratorWardDailyScreen(
                    onNavigateBack = { homeNavController.popBackStack() }
                )
            }

            composable(
                route = HomeTabRoutes.WARD_STATISTICS_ROUTE,
                arguments = listOf(
                    navArgument(HomeTabRoutes.WARD_ID_ARG) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val wardId = backStackEntry.arguments?.getLong(HomeTabRoutes.WARD_ID_ARG) ?: return@composable
                WardStatisticsScreen(
                    onNavigateBack = { homeNavController.popBackStack() },
                    onNavigateToThresholds = {
                        homeNavController.navigate(HomeTabRoutes.wardThresholds(wardId))
                    }
                )
            }

            composable(
                route = HomeTabRoutes.WARD_THRESHOLDS_ROUTE,
                arguments = listOf(
                    navArgument(HomeTabRoutes.WARD_ID_ARG) { type = NavType.LongType }
                )
            ) {
                WardThresholdsScreen(
                    onNavigateBack = { homeNavController.popBackStack() }
                )
            }

            composable(
                route = HomeTabRoutes.SCHEDULE_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument(HomeTabRoutes.WARD_ID_ARG) { type = NavType.LongType },
                    navArgument(HomeTabRoutes.SCHEDULE_ID_ARG) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val wardId = backStackEntry.arguments?.getLong(HomeTabRoutes.WARD_ID_ARG) ?: return@composable
                val scheduleId = backStackEntry.arguments?.getLong(HomeTabRoutes.SCHEDULE_ID_ARG) ?: return@composable

                ScheduleDetailScreen(
                    onNavigateBack = { homeNavController.popBackStack() },
                    onNavigateToCreateTask = {
                        homeNavController.navigate(HomeTabRoutes.taskEditorCreate(wardId, scheduleId))
                    },
                    onNavigateToEditTask = { taskId ->
                        homeNavController.navigate(HomeTabRoutes.taskEditorEdit(wardId, scheduleId, taskId))
                    }
                )
            }

            composable(
                route = HomeTabRoutes.TASK_EDITOR_ROUTE,
                arguments = listOf(
                    navArgument(HomeTabRoutes.WARD_ID_ARG) { type = NavType.LongType },
                    navArgument(HomeTabRoutes.SCHEDULE_ID_ARG) { type = NavType.LongType },
                    navArgument(HomeTabRoutes.TASK_ID_ARG) { type = NavType.LongType }
                )
            ) {
                TaskEditorScreen(
                    onNavigateBack = { homeNavController.popBackStack() }
                )
            }

            composable(HomeTabRoutes.ADD_WARD) {
                AddWardScreen(
                    onNavigateBack = { homeNavController.popBackStack() }
                )
            }

            composable(HomeTabRoutes.SCHEDULE) {
                ScheduleTabRouterScreen(
                    onNavigateToWardSchedules = { wardId ->
                        homeNavController.navigate(HomeTabRoutes.wardSchedules(wardId))
                    }
                )
            }
            composable(HomeTabRoutes.NOTIFICATIONS) {
                NotificationsScreen()
            }
            composable(HomeTabRoutes.STATISTICS) {
                StatisticsScreen(
                    onNavigateToWardStatistics = { wardId ->
                        homeNavController.navigate(HomeTabRoutes.wardStatistics(wardId))
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    navController: androidx.navigation.NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val tabs = listOf(
        BottomTab(HomeTabRoutes.SCHEDULE, "Расписание", Icons.Default.CalendarMonth),
        BottomTab(HomeTabRoutes.STATISTICS, "Статистика", Icons.Default.BarChart),
        BottomTab(HomeTabRoutes.NOTIFICATIONS, "Уведомления", Icons.Default.Notifications),
        BottomTab(HomeTabRoutes.PROFILE, "Профиль", Icons.Default.Person)
    )

    NavigationBar {
        tabs.forEach { tab ->
            val isActive = when (tab.route) {
                HomeTabRoutes.PROFILE -> currentRoute in HomeTabRoutes.PROFILE_TAB_ROUTES
                HomeTabRoutes.SCHEDULE -> currentRoute in HomeTabRoutes.SCHEDULE_TAB_ROUTES
                HomeTabRoutes.STATISTICS -> currentRoute in HomeTabRoutes.STATISTICS_TAB_ROUTES
                else -> currentRoute == tab.route
            }

            NavigationBarItem(
                selected = isActive,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.title) }
            )
        }
    }
}

private data class BottomTab(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
private fun ComingSoonScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Функциональность",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}