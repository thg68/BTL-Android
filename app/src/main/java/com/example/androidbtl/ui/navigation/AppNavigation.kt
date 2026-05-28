package com.example.androidbtl.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidbtl.ui.components.AppBottomNavBar
import com.example.androidbtl.ui.components.Screen
import com.example.androidbtl.ui.screens.*
import com.example.androidbtl.utils.NotificationHelper
import com.example.androidbtl.viewmodel.PosViewModel
import com.google.firebase.messaging.FirebaseMessaging

private const val NAV_ANIM_DURATION = 280

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val posViewModel: PosViewModel = viewModel()
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    var isCustomerRole by remember { mutableStateOf<Boolean?>(null) }
    var customerTableId by remember { mutableStateOf("") }
    var hasBeenServing by remember { mutableStateOf(false) }
    
    val tables by posViewModel.tables.collectAsState()
    val pendingItemCount by posViewModel.pendingItemCount.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val navBackStackEntry: NavBackStackEntry? by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTable = tables.find { it.id == customerTableId }
    val isTableServing = currentTable?.status == "Đang phục vụ"
    val isTableCleared = currentTable?.status == "Trống"

    LaunchedEffect(isTableServing) {
        if (isTableServing) {
            hasBeenServing = true
        }
    }

    LaunchedEffect(isTableCleared, isCustomerRole) {
        if (isCustomerRole == true && customerTableId.isNotEmpty() && isTableCleared && hasBeenServing && currentRoute != Screen.Login.route) {
            scope.launch {
                snackbarHostState.showSnackbar("Cảm ơn quý khách! Bạn sẽ tự động đăng xuất sau 30 giây.")
            }
            delay(30000L)
            if (isCustomerRole == true && customerTableId.isNotEmpty()) {
                val stillCleared = posViewModel.tables.value.find { it.id == customerTableId }?.status == "Trống"
                if (stillCleared) {
                    isCustomerRole = null
                    customerTableId = ""
                    hasBeenServing = false
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            }
        }
    }

    LaunchedEffect(customerTableId, isCustomerRole) {
        posViewModel.dishReadyEvent.collect { (tableId, message) ->
            if (isCustomerRole == true && tableId == customerTableId) {
                notificationHelper.showNotification(title = "Món ăn đã sẵn sàng!", message = message)
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    LaunchedEffect(isCustomerRole) {
        posViewModel.newOrderEvent.collect { message ->
            if (isCustomerRole == false) {
                notificationHelper.showNotification(title = "Thông báo mới", message = message)
            }
        }
    }

    val slideEnter: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.EnterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION)) + fadeIn(tween(NAV_ANIM_DURATION))
    }
    val slideExit: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.ExitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(NAV_ANIM_DURATION)) + fadeOut(tween(NAV_ANIM_DURATION))
    }
    val slidePopEnter: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.EnterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION)) + fadeIn(tween(NAV_ANIM_DURATION))
    }
    val slidePopExit: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.ExitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(NAV_ANIM_DURATION)) + fadeOut(tween(NAV_ANIM_DURATION))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isCustomerRole != null) {
                    AppBottomNavBar(
                        navController = navController,
                        isCustomer = isCustomerRole == true,
                        customerTableId = customerTableId,
                        pendingItemCount = pendingItemCount
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = slideEnter,
                exitTransition = slideExit,
                popEnterTransition = slidePopEnter,
                popExitTransition = slidePopExit
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = posViewModel,
                        onCustomerLogin = { tableId ->
                            isCustomerRole = true
                            customerTableId = tableId
                            hasBeenServing = false
                            posViewModel.ensureOrderForTable(tableId)
                            
                            // Lấy FCM Token và lưu vào bàn trên Firestore
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    posViewModel.updateTableFcmToken(tableId, token)
                                }
                            }

                            navController.navigate(Screen.CusHome.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                        },
                        onStaffLogin = {
                            isCustomerRole = false
                            navController.navigate(Screen.Tables.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                        }
                    )
                }
                
                // Các màn hình khác giữ nguyên...
                composable(Screen.Tables.route) { TableManagementScreen(viewModel = posViewModel, onTableClick = { id, _ -> navController.navigate("staff_pos/$id") }, onLogout = { isCustomerRole = null; navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }) }
                composable(Screen.KDS.route) { KitchenDisplayScreen(viewModel = posViewModel) }
                composable(Screen.StaffMenu.route) { StaffMenuScreen(viewModel = posViewModel) }
                composable(Screen.Billing.route) { BillingScreen(viewModel = posViewModel) }
                composable(Screen.Revenue.route) { RevenueScreen(viewModel = posViewModel) }
                composable(route = "staff_pos/{tableId}", arguments = listOf(navArgument("tableId") { type = NavType.StringType })) { backStackEntry -> val id = backStackEntry.arguments?.getString("tableId") ?: ""; POSOrderScreen(tableId = id, viewModel = posViewModel, onNavigateToBooking = { navController.navigate("cus_booking/$id") }, onBack = { navController.popBackStack() }, onShowMessage = showMessage) }
                composable(Screen.CusHome.route) { HomeScreen(tableId = customerTableId, viewModel = posViewModel, onNavigateToMenu = { navController.navigate(Screen.CusMenu.route) }, onNavigateToBill = { navController.navigate("cus_bill/$customerTableId") }, onNavigateToProfile = { navController.navigate(Screen.CusProfile.route) }, onShowMessage = showMessage) }
                composable(Screen.CusMenu.route) { POSOrderScreen(tableId = customerTableId, viewModel = posViewModel, onNavigateToBooking = { navController.navigate("cus_booking/$customerTableId") }, onBack = { navController.popBackStack() }, onShowMessage = showMessage) }
                composable(Screen.CusOffers.route) { OffersScreen() }
                composable(route = "cus_booking/{tableId}", arguments = listOf(navArgument("tableId") { type = NavType.StringType })) { backStackEntry -> val id = backStackEntry.arguments?.getString("tableId") ?: ""; BookingScreen(tableId = id, viewModel = posViewModel, onBack = { navController.popBackStack() }, onShowMessage = showMessage) }
                composable(route = "cus_bill/{tableId}", arguments = listOf(navArgument("tableId") { type = NavType.StringType })) { backStackEntry -> val id = backStackEntry.arguments?.getString("tableId") ?: ""; BillScreen(tableId = id, viewModel = posViewModel) }
                composable(Screen.CusProfile.route) { ProfileScreen(tableId = customerTableId, viewModel = posViewModel, onLogout = { isCustomerRole = null; customerTableId = ""; navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }) }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Snackbar(snackbarData = data, containerColor = Color(0xFF323232), contentColor = Color.White)
        }
    }
}
