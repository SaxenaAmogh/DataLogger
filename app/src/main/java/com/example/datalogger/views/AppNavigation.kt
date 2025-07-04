package com.example.datalogger.views

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "home" // Start with the TransactionPage,
    ) {
        composable("home") {
            HomePage(navController)
        }
    }
}