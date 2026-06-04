package com.example.androidbtl.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.androidbtl.data.models.NotificationItem
import com.example.androidbtl.ui.components.AppBottomNavBar
import com.example.androidbtl.ui.components.Screen
import com.example.androidbtl.ui.screens.BillingScreen
import com.example.androidbtl.ui.screens.BillScreen
import com.example.androidbtl.ui.screens.BookingScreen
import com.example.androidbtl.ui.screens.HomeScreen
import com.example.androidbtl.ui.screens.KitchenDisplayScreen
import com.example.androidbtl.ui.screens.LoginScreen
import com.example.androidbtl.ui.screens.OffersScreen
import com.example.androidbtl.ui.screens.POSOrderScreen
import com.example.androidbtl.ui.screens.ProfileScreen
import com.example.androidbtl.ui.screens.RevenueScreen
import com.example.androidbtl.ui.screens.StaffMenuScreen
import com.example.androidbtl.ui.screens.TableManagementScreen
import com.example.androidbtl.utils.NotificationHelper
import com.example.androidbtl.viewmodel.PosViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TABLE_DEEP_LINK = "androidbtl://table/{tableId}?code={accessCode}"

/**
 * Root UI của app: giữ role hiện tại, điều phối navigation, snackbar,
 * thông báo và luồng tự đăng xuất sau khi khách đã thanh toán.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val posViewModel: PosViewModel = viewModel()
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    // Session UI cấp app: null = chưa đăng nhập, true = khách, false = nhân viên.
    var isCustomerRole by remember { mutableStateOf<Boolean?>(null) }
    var customerTableId by remember { mutableStateOf("") }
    var hasBeenServing by remember { mutableStateOf(false) }
    var hasReportedPayment by remember { mutableStateOf(false) }
    var staffTabRoute by remember { mutableStateOf(Screen.Tables.route) }

    val tables by posViewModel.tables.collectAsStateWithLifecycle()
    val pendingItemCount by posViewModel.pendingItemCount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    // Core login rule:
    // - Nhập tay chỉ được vào bàn trống, tránh khách mới chiếm phiên của bàn đang phục vụ.
    // - QR có access code là "vé vào phiên bàn" do nhân viên mở, nên được vào lại bàn đang phục vụ.
    // - Access code chỉ dùng để phân biệt login QR với nhập tay; trạng thái hợp lệ vẫn kiểm tra ở Firestore.
    val loginCustomerToTable: (String, String?, Boolean) -> Unit = login@{ tableId, accessCode, fromQr ->
        val normalizedTableId = tableId.trim()
        val normalizedAccessCode = accessCode.orEmpty()
        val hasQrAccess = fromQr && normalizedAccessCode.isNotBlank()
        val table = posViewModel.tables.value.find { it.id == normalizedTableId }
        when {
            normalizedTableId.isBlank() -> {
                showMessage("Vui lòng chọn bàn hợp lệ.")
                return@login
            }
            table == null -> {
                showMessage("Không tìm thấy bàn $normalizedTableId.")
                return@login
            }
            table.status == "Đã đặt" -> {
                showMessage("Bàn $normalizedTableId đã được đặt trước.")
                return@login
            }
            table.status == "Đang phục vụ" && !hasQrAccess -> {
                showMessage("Bàn $normalizedTableId đang có khách sử dụng.")
                return@login
            }
        }

        // Sau khi qua validation, lưu session khách ở tầng navigation để toàn bộ tab dùng chung tableId.
        isCustomerRole = true
        customerTableId = normalizedTableId
        hasBeenServing = false
        hasReportedPayment = false
        // Bảo đảm bàn có order mở trước khi khách vào menu, nếu chưa có thì ViewModel sẽ tạo mới.
        posViewModel.ensureOrderForTable(normalizedTableId)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Token được lưu vào document bàn để bếp gửi push khi món chuyển sang Done.
                posViewModel.updateTableFcmToken(normalizedTableId, token)
            }
        }

        navController.navigate(Screen.CusHome.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    val navBackStackEntry: NavBackStackEntry? by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentTable = tables.find { it.id == customerTableId }
    val isTableServing = currentTable?.status == "Đang phục vụ"
    val isTableCleared = currentTable?.status == "Trống"

    // Click thông báo của nhân viên sẽ mở đúng tab liên quan:
    // - payment_* hoặc targetRoute=billing -> tab xác nhận thanh toán.
    // - call_* -> tab sơ đồ bàn để nhân viên biết bàn nào gọi.
    // - "món mới" -> tab bếp/KDS để xử lý món vừa gửi xuống.
    val openStaffNotificationTarget: (NotificationItem) -> Unit = { notification ->
        val targetRoute = when {
            notification.targetRoute.isNotBlank() -> notification.targetRoute
            notification.id.startsWith("payment_") -> Screen.Billing.route
            notification.id.startsWith("call_") -> Screen.Tables.route
            notification.message.contains("món mới", ignoreCase = true) -> Screen.KDS.route
            else -> Screen.Tables.route
        }
        staffTabRoute = targetRoute
        if (currentRoute != Screen.Tables.route) {
            navController.navigate(Screen.Tables.route) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(isTableServing) {
        if (isTableServing) hasBeenServing = true
    }

    // Chỉ tự đăng xuất khách sau khi đủ 4 điều kiện:
    // 1. Khách đang đăng nhập.
    // 2. Bàn đã từng ở trạng thái phục vụ, tránh logout nhầm lúc mới vào.
    // 3. Khách đã bấm báo thanh toán.
    // 4. Nhân viên đã xác nhận, closeOrder trả bàn về Trống.
    LaunchedEffect(isTableCleared, hasReportedPayment, isCustomerRole) {
        if (isCustomerRole == true && customerTableId.isNotEmpty() && isTableCleared && hasBeenServing && hasReportedPayment && currentRoute != Screen.Login.route) {
            scope.launch {
                snackbarHostState.showSnackbar("Cảm ơn quý khách! Bạn sẽ tự động đăng xuất sau 30 giây.")
            }
            delay(30000L) // Chờ 30 giây để khách đọc thông báo trước khi tự đăng xuất.
            if (isCustomerRole == true && customerTableId.isNotEmpty()) {
                // Sau 30 giây vẫn kiểm tra lại Firestore state để không logout sai nếu bàn bị mở lại.
                val stillCleared = posViewModel.tables.value.find { it.id == customerTableId }?.status == "Trống"
                if (stillCleared && hasReportedPayment) {
                    isCustomerRole = null
                    customerTableId = ""
                    hasBeenServing = false
                    hasReportedPayment = false
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

    val noEnter: () -> EnterTransition = { EnterTransition.None }
    val noExit: () -> ExitTransition = { ExitTransition.None }

    //Scaffold tạo layout chính. BottomBar chỉ hiện khi đã biết role.
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isCustomerRole != null) {
                    AppBottomNavBar(
                        navController = navController,
                        isCustomer = isCustomerRole == true,
                        customerTableId = customerTableId,
                        pendingItemCount = pendingItemCount,
                        staffCurrentRoute = staffTabRoute,
                        onStaffTabSelected = { screen -> staffTabRoute = screen.route }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { noEnter() },
                exitTransition = { noExit() },
                popEnterTransition = { noEnter() },
                popExitTransition = { noExit() }
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = posViewModel,
                        onCustomerLogin = { tableId, accessCode, fromQr -> loginCustomerToTable(tableId, accessCode, fromQr) },
                        onStaffLogin = {
                            isCustomerRole = false
                            customerTableId = ""
                            hasBeenServing = false
                            hasReportedPayment = false
                            staffTabRoute = Screen.Tables.route
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    posViewModel.registerStaffFcmToken(task.result)
                                }
                            }
                            navController.navigate(Screen.Tables.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    route = "open_table/{tableId}?code={accessCode}",
                    arguments = listOf(
                        navArgument("tableId") { type = NavType.StringType },
                        navArgument("accessCode") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    ),
                    deepLinks = listOf(navDeepLink { uriPattern = TABLE_DEEP_LINK })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("tableId").orEmpty()
                    val accessCode = backStackEntry.arguments?.getString("accessCode")
                    LaunchedEffect(id, accessCode, tables.isNotEmpty()) {
                        if (id.isNotBlank() && tables.isNotEmpty()) {
                            loginCustomerToTable(id, accessCode, accessCode.isNullOrBlank().not())
                        }
                    }
                }

                composable(Screen.Tables.route) {
                    // Staff tabs render trong route gốc "tables" để chuyển tab nhanh và giữ state tốt hơn.
                    when (staffTabRoute) {
                        Screen.KDS.route -> KitchenDisplayScreen(viewModel = posViewModel, onNotificationClick = openStaffNotificationTarget)
                        Screen.Billing.route -> BillingScreen(viewModel = posViewModel, onNotificationClick = openStaffNotificationTarget)
                        Screen.StaffMenu.route -> StaffMenuScreen(viewModel = posViewModel, onNotificationClick = openStaffNotificationTarget)
                        Screen.Revenue.route -> RevenueScreen(viewModel = posViewModel)
                        else -> TableManagementScreen(
                            viewModel = posViewModel,
                            onLogout = {
                                isCustomerRole = null
                                customerTableId = ""
                                staffTabRoute = Screen.Tables.route
                                hasBeenServing = false
                                hasReportedPayment = false
                                navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                            },
                            onNotificationClick = openStaffNotificationTarget
                        )
                    }
                }
                composable(Screen.KDS.route) { KitchenDisplayScreen(viewModel = posViewModel, onNotificationClick = openStaffNotificationTarget) }
                composable(Screen.StaffMenu.route) { StaffMenuScreen(viewModel = posViewModel, onNotificationClick = openStaffNotificationTarget) }
                composable(Screen.Billing.route) { BillingScreen(viewModel = posViewModel, onNotificationClick = openStaffNotificationTarget) }
                composable(Screen.Revenue.route) { RevenueScreen(viewModel = posViewModel) }
                composable(
                    route = "staff_pos/{tableId}",
                    arguments = listOf(navArgument("tableId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("tableId").orEmpty()
                    POSOrderScreen(
                        tableId = id,
                        viewModel = posViewModel,
                        onNavigateToBooking = { navController.navigate("cus_booking/$id") },
                        onBack = { navController.popBackStack() },
                        onShowMessage = showMessage
                    )
                }
                composable(Screen.CusHome.route) {
                    HomeScreen(
                        tableId = customerTableId,
                        viewModel = posViewModel,
                        onNavigateToMenu = { navController.navigate(Screen.CusMenu.route) },
                        onNavigateToBill = { navController.navigate("cus_bill/$customerTableId") },
                        onNavigateToProfile = { navController.navigate(Screen.CusProfile.route) },
                        onShowMessage = showMessage
                    )
                }
                composable(Screen.CusMenu.route) {
                    POSOrderScreen(
                        tableId = customerTableId,
                        viewModel = posViewModel,
                        onNavigateToBooking = { navController.navigate("cus_booking/$customerTableId") },
                        onBack = { navController.popBackStack() },
                        onShowMessage = showMessage
                    )
                }
                composable(Screen.CusOffers.route) { OffersScreen() }
                composable(
                    route = "cus_booking/{tableId}",
                    arguments = listOf(navArgument("tableId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("tableId").orEmpty()
                    BookingScreen(tableId = id, viewModel = posViewModel, onBack = { navController.popBackStack() }, onShowMessage = showMessage)
                }
                composable(
                    route = "cus_bill/{tableId}",
                    arguments = listOf(navArgument("tableId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("tableId").orEmpty()
                    BillScreen(
                        tableId = id,
                        viewModel = posViewModel,
                        onShowMessage = showMessage,
                        onPaymentReported = { hasReportedPayment = true }
                    )
                }
                composable(Screen.CusProfile.route) {
                    ProfileScreen(
                        tableId = customerTableId,
                        viewModel = posViewModel,
                        onLogout = {
                            isCustomerRole = null
                            customerTableId = ""
                            hasBeenServing = false
                            hasReportedPayment = false
                            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
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
