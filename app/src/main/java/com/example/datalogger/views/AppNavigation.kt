package com.example.datalogger.views

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.datalogger.viewmodel.BleViewModel

//Composable to handle navigation between different screens in the app
@Composable
fun AppNavigation(navController: NavHostController) {

    val bleViewModel: BleViewModel = viewModel()

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
            ConnectionScreen(navController, bleViewModel)
        }
        composable("connected"){
            ConnectedPage(navController, bleViewModel)
        }
    }
}