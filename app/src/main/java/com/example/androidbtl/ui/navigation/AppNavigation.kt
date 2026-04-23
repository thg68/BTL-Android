package com.example.androidbtl.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidbtl.ui.components.AppBottomNavBar
import com.example.androidbtl.ui.components.Screen
import com.example.androidbtl.ui.screens.*
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val posViewModel: PosViewModel = viewModel()
    
    // Auth State: null = not logged in, true = Customer, false = Staff
    var isCustomerRole by remember { mutableStateOf<Boolean?>(null) }
    // Store Customer current table Id globally
    var customerTableId by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { 
            if (isCustomerRole != null) {
                AppBottomNavBar(navController = navController, isCustomer = isCustomerRole == true)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // LOGIN SCREEN
            composable(Screen.Login.route) {
                LoginScreen(
                    onCustomerLogin = { tableId ->
                        isCustomerRole = true
                        customerTableId = tableId
                        // Create order if not exist, then route to customer home
                        posViewModel.createOrderForTable(tableId)
                        navController.navigate(Screen.CusHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onStaffLogin = {
                        isCustomerRole = false
                        navController.navigate(Screen.Tables.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- STAFF ROUTES ---
            composable(Screen.Tables.route) {
                TableManagementScreen(viewModel = posViewModel) { tableId, status ->
                    navController.navigate("staff_pos/$tableId")
                }
            }
            composable(
                route = "staff_pos/{tableId}",
                arguments = listOf(navArgument("tableId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId") ?: ""
                POSOrderScreen(tableId = tableId, viewModel = posViewModel) {
                    navController.popBackStack()
                }
            }
            composable(Screen.KDS.route) {
                KitchenDisplayScreen(viewModel = posViewModel)
            }
            composable(Screen.Billing.route) {
                BillingScreen(viewModel = posViewModel)
            }

            // --- CUSTOMER ROUTES ---
            composable(Screen.CusHome.route) {
                HomeScreen(
                    onNavigateToMenu = {
                        navController.navigate(Screen.CusMenu.route) {
                            popUpTo(Screen.CusHome.route)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.CusMenu.route) {
                // Reuse POSOrderScreen for customer menu, but simplified without Back button if bottom tab
                // We'll just pass customerTableId
                POSOrderScreen(tableId = customerTableId, viewModel = posViewModel) { }
            }
            composable(Screen.CusProfile.route) {
                ProfileScreen()
            }
        }
    }
}
