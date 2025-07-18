package com.example.datalogger.views

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "home"
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
    }
}