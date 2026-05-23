package ru.akuzyukhin.orientir.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.akuzyukhin.orientir.feature.auth.ui.login.LoginScreen
import ru.akuzyukhin.orientir.feature.auth.ui.register.RegisterScreen
import ru.akuzyukhin.orientir.feature.auth.ui.splash.SplashScreen
import ru.akuzyukhin.orientir.feature.home.ui.HomeScreen

/** Корневой граф навигации приложения */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OrientirNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = OrientirRoutes.SPLASH
    ) {
        composable(OrientirRoutes.SPLASH) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(OrientirRoutes.HOME) {
                        popUpTo(OrientirRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(OrientirRoutes.LOGIN) {
                        popUpTo(OrientirRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = OrientirRoutes.LOGIN_ROUTE,
            arguments = listOf(
                navArgument(OrientirRoutes.LOGIN_ARG_PHONE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(OrientirRoutes.HOME) {
                        popUpTo(OrientirRoutes.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(OrientirRoutes.REGISTER)
                }
            )
        }

        composable(OrientirRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate(OrientirRoutes.HOME) {
                        popUpTo(OrientirRoutes.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = { phone ->
                    val route = OrientirRoutes.loginWithPhone(phone)
                    navController.navigate(route) {
                        popUpTo(OrientirRoutes.LOGIN_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(OrientirRoutes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(OrientirRoutes.LOGIN) {
                        popUpTo(OrientirRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}