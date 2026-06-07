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
import kotlinx.coroutines.launch

private const val TABLE_DEEP_LINK = "androidbtl://table/{tableId}?code={accessCode}"

/**
 * Root UI của app.
 *
 * File này là nơi nối các phần core lại với nhau:
 * - Giữ session tạm trên UI: chưa đăng nhập, khách, hoặc nhân viên.
 * - Điều phối route cho khách/nhân viên và deep link từ QR bàn.
 * - Gửi snackbar dùng chung cho các màn con.
 * - Mở đúng tab nhân viên khi bấm notification.
 * - Tự đăng xuất khách khi nhân viên đóng phiên bàn.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val posViewModel: PosViewModel = viewModel()
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }

    // Session UI cấp app:
    // - null: chưa chọn vai trò hoặc đã logout.
    // - true: khách đang dùng app theo một tableId cụ thể.
    // - false: nhân viên đang dùng cụm tab vận hành.
    //
    // Session này chỉ điều khiển UI/navigation. Trạng thái thật của bàn vẫn nằm ở Firestore
    // thông qua RestaurantTable.status, accessCode và các order Open/Closed.
    var isCustomerRole by remember { mutableStateOf<Boolean?>(null) }
    var customerTableId by remember { mutableStateOf("") }
    var hasBeenServing by remember { mutableStateOf(false) }
    var staffTabRoute by remember { mutableStateOf(Screen.Tables.route) }

    val tables by posViewModel.tables.collectAsStateWithLifecycle()
    val pendingItemCount by posViewModel.pendingItemCount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    // Luật login khách:
    // - Nhập tay chỉ cho vào bàn chưa phục vụ để tránh khách mới vào nhầm bàn đang có người.
    // - QR có accessCode được coi là vé vào phiên bàn hiện tại, nên khách cùng bàn có thể quét lại.
    // - Bàn đã đặt vẫn bị chặn để nhân viên chủ động mở bàn đúng thời điểm.
    //
    // Lưu ý: hiện tại chỉ kiểm tra accessCode có tồn tại trong deep link để phân biệt nguồn QR.
    // Nếu muốn bảo mật chặt hơn, nên so khớp accessCode với document tables/{tableId}.
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

        isCustomerRole = true
        customerTableId = normalizedTableId
        hasBeenServing = false

        // Đảm bảo mỗi bàn có đúng một order Open trước khi khách vào menu.
        // Nếu order đã có, ViewModel chỉ cập nhật lại trạng thái bàn là Đang phục vụ.
        posViewModel.ensureOrderForTable(normalizedTableId)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                // Token FCM được gắn vào document bàn, không gắn vào user.
                // Nhờ vậy bếp chỉ cần biết tableId là gửi được push về thiết bị khách đang ngồi bàn đó.
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

    // Notification của nhân viên phải điều hướng về đúng khu vực xử lý:
    // - targetRoute được ưu tiên vì ViewModel đã biết nghiệp vụ khi tạo NotificationItem.
    // - payment_* mở tab xác nhận thanh toán.
    // - call_* mở sơ đồ bàn.
    // - thông báo món mới mở KDS để bếp xử lý ngay.
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

    // Tự logout khách khi phiên bàn kết thúc:
    // - closeOrder() chỉ đóng hóa đơn, không làm khách rời app.
    // - closeTable() đưa table.status về Trống và xóa accessCode/fcmToken.
    // - hasBeenServing giúp tránh logout nhầm lúc app vừa mở, khi dữ liệu bàn chưa đồng bộ xong.
    LaunchedEffect(isTableCleared, isCustomerRole) {
        if (isCustomerRole == true && customerTableId.isNotEmpty() && isTableCleared && hasBeenServing && currentRoute != Screen.Login.route) {
            scope.launch {
                snackbarHostState.showSnackbar("Bàn đã được nhân viên đóng. Bạn đã được đăng xuất.")
            }
            isCustomerRole = null
            customerTableId = ""
            hasBeenServing = false
            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
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
                    // Cụm tab nhân viên render trong cùng route gốc "tables".
                    // Cách này tránh push nhiều route vào back stack khi đổi tab, nên cảm giác chuyển tab nhanh hơn
                    // và state cục bộ của từng tab không bị reset liên tục như khi navigate route riêng.
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
                        onShowMessage = showMessage
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
