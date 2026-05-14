package com.example.androidbtl.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
    
    var isCustomerRole by remember { mutableStateOf<Boolean?>(null) }
    var customerTableId by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isCustomerRole != null) {
                    AppBottomNavBar(
                        navController = navController,
                        isCustomer = isCustomerRole == true,
                        customerTableId = customerTableId
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = Modifier.padding(innerPadding)
            ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onCustomerLogin = { tableId ->
                        isCustomerRole = true
                        customerTableId = tableId
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

            // STAFF
            composable(Screen.Tables.route) {
                TableManagementScreen(viewModel = posViewModel) { tableId, _ ->
                    navController.navigate("staff_pos/$tableId")
                }
            }
            composable(
                route = "staff_pos/{tableId}",
                arguments = listOf(navArgument("tableId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId") ?: ""
                POSOrderScreen(
                    tableId = tableId,
                    viewModel = posViewModel,
                    onNavigateToBooking = { navController.navigate("cus_booking/$tableId") },
                    onBack = { navController.popBackStack() },
                    onShowMessage = showMessage
                )
            }

            // CUSTOMER
            composable(Screen.CusHome.route) {
                HomeScreen(onNavigateToMenu = { navController.navigate(Screen.CusMenu.route) })
            }
            composable(Screen.CusMenu.route) {
                POSOrderScreen(
                    tableId = customerTableId,
                    viewModel = posViewModel,
                    onNavigateToBooking = {
                        navController.navigate("cus_booking/$customerTableId")
                    },
                    onBack = { navController.popBackStack() },
                    onShowMessage = showMessage
                )
            }
            composable(
                route = "cus_booking/{tableId}",
                arguments = listOf(navArgument("tableId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId") ?: ""
                BookingScreen(
                    tableId = tableId,
                    viewModel = posViewModel,
                    onBack = { navController.popBackStack() },
                    onShowMessage = showMessage
                )
            }
            composable(
                route = "cus_bill/{tableId}",
                arguments = listOf(navArgument("tableId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId") ?: ""
                BillScreen(tableId = tableId, viewModel = posViewModel)
            }

            composable(Screen.KDS.route) { KitchenDisplayScreen(viewModel = posViewModel) }
            composable(Screen.Billing.route) { BillingScreen(viewModel = posViewModel) }
            composable(Screen.CusProfile.route) { ProfileScreen() }
        }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF323232),
                contentColor = Color.White
            )
        }
    }
}
