package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.Order
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.LocalThemeIsDark
import com.example.androidbtl.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    tableId: String,
    viewModel: PosViewModel,
    onLogout: () -> Unit
) {
    val isDarkState = LocalThemeIsDark.current
    var isDark by isDarkState
    val closedOrders by viewModel.closedOrders.collectAsState()
    val myOrders = closedOrders.filter { it.tableId == tableId }
    var showHistory by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tài Khoản",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BrandYellow.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Avatar",
                    tint = BrandYellow,
                    modifier = Modifier.size(80.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Khách hàng Bàn $tableId",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Đã hoàn tất ${myOrders.size} đơn",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            ProfileMenuItem(
                title = "Lịch sử giao dịch",
                icon = Icons.Filled.History,
                trailing = "${myOrders.size}"
            ) { showHistory = true }

            ProfileMenuItem(
                title = "Ưu đãi của tôi",
                icon = Icons.Filled.Star
            ) { }

            ProfileMenuToggle(
                title = if (isDark) "Chế độ tối" else "Chế độ sáng",
                icon = if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                checked = isDark,
                onCheckedChange = { isDark = it }
            )

            ProfileMenuItem(
                title = "Đăng xuất",
                icon = Icons.AutoMirrored.Filled.Logout,
                tintOverride = MaterialTheme.colorScheme.tertiary
            ) { showLogoutDialog = true }
        }
    }

    if (showHistory) {
        OrderHistoryDialog(orders = myOrders, onDismiss = { showHistory = false })
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi bàn $tableId?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Đăng xuất", color = MaterialTheme.colorScheme.tertiary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Huỷ") }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    trailing: String? = null,
    tintOverride: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tintOverride ?: BrandYellow)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = tintOverride ?: MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                Surface(
                    color = BrandYellow.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        trailing,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandYellow
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuToggle(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = BrandYellow)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BrandYellow,
                    checkedTrackColor = BrandYellow.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun OrderHistoryDialog(orders: List<Order>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lịch sử đơn hàng") },
        text = {
            if (orders.isEmpty()) {
                Text("Bạn chưa có đơn hàng đã hoàn tất.")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(orders) { order -> HistoryItemRow(order) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng", color = BrandYellow) }
        }
    )
}

@Composable
private fun HistoryItemRow(order: Order) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Receipt,
            contentDescription = null,
            tint = BrandYellow,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Bàn ${order.tableId}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                formatter.format(Date(order.timestamp)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            "${"%,.0f".format(order.totalAmount)}đ",
            fontWeight = FontWeight.ExtraBold,
            color = BrandYellow,
            fontSize = 14.sp
        )
    }
}
