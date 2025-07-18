package com.example.datalogger.views

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("signin") {
            SignInPage(navController)
        }
        composable("signup") {
            SignUpPage(navController)
        }
        composable("home") {
             HomePage(navController)
        }
        composable("connect") {
            ConnectionScreen(navController)
        }
        composable(
            route = "connected/{deviceName}",
            arguments = listOf(navArgument("deviceName") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: "ESP32"
            ConnectedPage(deviceName, navController)
        }
    }
}