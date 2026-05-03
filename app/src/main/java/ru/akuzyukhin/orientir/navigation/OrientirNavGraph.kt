package ru.akuzyukhin.orientir.navigation

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
                    android.util.Log.d("NavGraph", "register→login: phone='$phone', route='$route'")
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
                onNavigateToLogin = {
                    navController.navigate(OrientirRoutes.LOGIN) {
                        popUpTo(OrientirRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}