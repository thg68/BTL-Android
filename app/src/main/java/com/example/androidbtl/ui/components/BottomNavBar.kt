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

/**
 * Danh sách route chính của app.
 *
 * Mỗi Screen chứa đủ 3 thông tin mà navigation bar cần:
 * - route: chuỗi NavController dùng để điều hướng.
 * - icon: icon Material hiển thị ở bottom bar.
 * - title: nhãn hiển thị cho người dùng.
 *
 * Gom route vào đây giúp AppNavigation, BottomNavBar và các notification không phải hard-code
 * nhiều chuỗi route rải rác, giảm lỗi gõ sai route khi mở tab.
 */
sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Login : Screen("login", Icons.Filled.AccountCircle, "Đăng nhập")
    
    object Tables : Screen("tables", Icons.Filled.TableRestaurant, "Bàn")
    object KDS : Screen("kds", Icons.Filled.Restaurant, "Bếp")
    object StaffMenu : Screen("staff_menu", Icons.Filled.Inventory, "Món ăn")
    object Billing : Screen("billing", Icons.Filled.Payments, "Xác nhận")
    object Revenue : Screen("revenue", Icons.Filled.BarChart, "Doanh thu")
    object StaffPOS : Screen("staff_pos/{tableId}", Icons.AutoMirrored.Filled.List, "Order") 

    object CusHome : Screen("cus_home", Icons.Filled.Home, "Trang chủ")
    object CusMenu : Screen("cus_menu", Icons.Filled.RestaurantMenu, "Thực đơn")
    object CusOffers : Screen("cus_offers", Icons.Filled.LocalOffer, "Ưu đãi")
    object CusBill : Screen("cus_bill/{tableId}", Icons.Filled.Receipt, "Hóa đơn")
    object CusBooking : Screen("cus_booking/{tableId}", Icons.AutoMirrored.Filled.List, "Giỏ hàng")
    object CusProfile : Screen("cus_profile", Icons.Filled.AccountCircle, "Tài khoản")
}

/**
 * Bottom navigation dùng chung cho khách và nhân viên.
 *
 * Có hai cách đổi màn:
 * - Khách dùng NavController thật vì các route khách có tham số tableId và cần back stack bình thường.
 * - Nhân viên dùng callback onStaffTabSelected để đổi state staffTabRoute trong AppNavigation.
 *   Nhờ vậy đổi tab nhân viên không tạo route mới liên tục, giảm delay và giữ state UI tốt hơn.
 */
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

    // Route có thể có tham số như cus_bill/{tableId}. Chỉ so sánh phần base trước dấu "/"
    // để item bottom bar vẫn selected đúng khi đang ở route chi tiết có tableId.
    val currentBase = remember(selectedRoute) { selectedRoute?.substringBefore("/") }

    // Login và màn POS chi tiết không dùng bottom bar.
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
                        // Nhân viên đổi tab bằng state, không navigate route mới.
                        // Đây là tối ưu quan trọng cho cảm giác chuyển tab nhanh trong app vận hành.
                        onStaffTabSelected(screen)
                        return@NavigationBarItem
                    }

                    // Hóa đơn khách cần tableId thật để BillScreen biết lấy order của bàn nào.
                    // Các tab khách còn lại dùng route cố định.
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
