package com.example.androidbtl.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    // Shared / System
    object Login : Screen("login", Icons.Filled.AccountCircle, "Đăng nhập")
    
    // Staff Screens
    object Tables : Screen("tables", Icons.Filled.TableRestaurant, "Sơ đồ bàn")
    object KDS : Screen("kds", Icons.Filled.Restaurant, "Bếp (KDS)")
    object Billing : Screen("billing", Icons.Filled.Payments, "Thu ngân")
    object StaffPOS : Screen("staff_pos/{tableId}", Icons.Filled.List, "Order") 

    // Customer Screens
    object CusHome : Screen("cus_home", Icons.Filled.Home, "Trang chủ")
    object CusMenu : Screen("cus_menu", Icons.Filled.RestaurantMenu, "Thực đơn")
    object CusBill : Screen("cus_bill/{tableId}", Icons.Filled.Receipt, "Hóa đơn")
    object CusBooking : Screen("cus_booking/{tableId}", Icons.Filled.List, "Giỏ hàng")
    object CusProfile : Screen("cus_profile", Icons.Filled.AccountCircle, "Tài khoản")
}

@Composable
fun AppBottomNavBar(navController: NavController, isCustomer: Boolean, customerTableId: String = "") {
    val items = if (isCustomer) {
        listOf(Screen.CusHome, Screen.CusMenu, Screen.CusBill, Screen.CusProfile)
    } else {
        listOf(Screen.Tables, Screen.KDS, Screen.Billing)
    }

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on login and deep POS screens
    if (currentRoute == Screen.Login.route) return
    if (currentRoute?.startsWith("staff_pos") == true) return

    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
    ) {
        items.forEach { screen ->
            val routeBase = screen.route.substringBefore("/")
            val currentBase = currentRoute?.substringBefore("/")
            
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
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
                    selectedIconColor = TextPrimary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = TextPrimary,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = BrandYellow
                )
            )
        }
    }
}
