package com.example.androidbtl.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
    object Billing : Screen("billing", Icons.Filled.Payments, "Thu ngân")
    object StaffPOS : Screen("staff_pos/{tableId}", Icons.AutoMirrored.Filled.List, "Order") 

    // Customer Screens
    object CusHome : Screen("cus_home", Icons.Filled.Home, "Trang chủ")
    object CusMenu : Screen("cus_menu", Icons.Filled.RestaurantMenu, "Thực đơn")
    object CusBill : Screen("cus_bill/{tableId}", Icons.Filled.Receipt, "Hóa đơn")
    object CusBooking : Screen("cus_booking/{tableId}", Icons.AutoMirrored.Filled.List, "Giỏ hàng")
    object CusProfile : Screen("cus_profile", Icons.Filled.AccountCircle, "Tài khoản")
}

@Composable
fun AppBottomNavBar(
    navController: NavController,
    isCustomer: Boolean,
    customerTableId: String = "",
    pendingItemCount: Int = 0
) {
    val items = if (isCustomer) {
        listOf(Screen.CusHome, Screen.CusMenu, Screen.CusBill, Screen.CusProfile)
    } else {
        listOf(Screen.Tables, Screen.KDS, Screen.StaffMenu, Screen.Billing)
    }

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on login and deep POS screens
    if (currentRoute == Screen.Login.route) return
    if (currentRoute?.startsWith("staff_pos") == true) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .shadow(20.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
    ) {
        items.forEach { screen ->
            val routeBase = screen.route.substringBefore("/")
            val currentBase = currentRoute?.substringBefore("/")

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
                    val targetRoute = if (screen == Screen.CusBill && customerTableId.isNotBlank()) {
                        "cus_bill/$customerTableId"
                    } else {
                        screen.route
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
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
