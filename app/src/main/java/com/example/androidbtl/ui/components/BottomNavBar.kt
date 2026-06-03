package com.example.androidbtl.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.androidbtl.ui.theme.BrandYellow

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    // Shared / System
    object Login : Screen("login", Icons.Filled.AccountCircle, "Đăng nhập")
    
    // Staff Screens
    object Tables : Screen("tables", Icons.Filled.TableRestaurant, "Bàn")
    object KDS : Screen("kds", Icons.Filled.Restaurant, "Bếp")
    object StaffMenu : Screen("staff_menu", Icons.Filled.Inventory, "Món ăn")
    object Billing : Screen("billing", Icons.Filled.Payments, "Xác nhận")
    object Revenue : Screen("revenue", Icons.Filled.BarChart, "Doanh thu")
    object StaffPOS : Screen("staff_pos/{tableId}", Icons.AutoMirrored.Filled.List, "Order") 

    // Customer Screens
    object CusHome : Screen("cus_home", Icons.Filled.Home, "Trang chủ")
    object CusMenu : Screen("cus_menu", Icons.Filled.RestaurantMenu, "Thực đơn")
    object CusOffers : Screen("cus_offers", Icons.Filled.LocalOffer, "Ưu đãi")
    object CusBill : Screen("cus_bill/{tableId}", Icons.Filled.Receipt, "Hóa đơn")
    object CusBooking : Screen("cus_booking/{tableId}", Icons.AutoMirrored.Filled.List, "Giỏ hàng")
    object CusProfile : Screen("cus_profile", Icons.Filled.AccountCircle, "Tài khoản")
}

@Composable
fun AppBottomNavBar(
    navController: NavController,
    isCustomer: Boolean,
    customerTableId: String = "",
    pendingItemCount: Int = 0,
    staffCurrentRoute: String? = null,
    onStaffTabSelected: ((Screen) -> Unit)? = null
) {
    val items = remember(isCustomer) {
        if (isCustomer) {
            listOf(Screen.CusHome, Screen.CusMenu, Screen.CusOffers, Screen.CusBill, Screen.CusProfile)
        } else {
            listOf(Screen.Tables, Screen.KDS, Screen.Billing, Screen.StaffMenu, Screen.Revenue)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val selectedRoute = if (!isCustomer && staffCurrentRoute != null) staffCurrentRoute else currentRoute
    val currentBase = remember(selectedRoute) { selectedRoute?.substringBefore("/") }

    // Hide bottom bar on login and deep POS screens
    if (currentRoute == Screen.Login.route) return
    if (currentRoute?.startsWith("staff_pos") == true) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(32.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
    ) {
        items.forEach { screen ->
            val routeBase = screen.route.substringBefore("/")

            NavigationBarItem(
                icon = {
                    if (screen == Screen.KDS && pendingItemCount > 0) {
                        BadgedBox(badge = {
                            Badge { Text(if (pendingItemCount > 99) "99+" else pendingItemCount.toString()) }
                        }) {
                            Icon(screen.icon, contentDescription = screen.title)
                        }
                    } else {
                        Icon(screen.icon, contentDescription = screen.title)
                    }
                },
                label = { Text(screen.title) },
                selected = currentBase == routeBase,
                onClick = {
                    if (currentBase == routeBase) return@NavigationBarItem

                    if (!isCustomer && onStaffTabSelected != null) {
                        onStaffTabSelected(screen)
                        return@NavigationBarItem
                    }

                    val targetRoute = if (screen == Screen.CusBill && customerTableId.isNotBlank()) {
                        "cus_bill/$customerTableId"
                    } else {
                        screen.route
                    }
                    val rootRoute = if (isCustomer) Screen.CusHome.route else Screen.Tables.route
                    navController.navigate(targetRoute) {
                        popUpTo(rootRoute) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = BrandYellow
                )
            )
        }
    }
}
