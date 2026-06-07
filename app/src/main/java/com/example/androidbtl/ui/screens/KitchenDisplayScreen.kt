package com.example.androidbtl.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.NotificationItem
import com.example.androidbtl.data.models.OrderItem
import com.example.androidbtl.ui.components.AsyncFoodImage
import com.example.androidbtl.ui.components.EmptyState
import com.example.androidbtl.ui.components.StaffNotificationBell
import com.example.androidbtl.ui.theme.ActionGreen
import com.example.androidbtl.ui.theme.ActionRed
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Kitchen Display System cho bếp.
 *
 * Màn này chỉ hiển thị các món đã rời khỏi giỏ Cart:
 * - Pending: khách/nhân viên vừa gửi xuống bếp.
 * - Cooking: bếp đang làm.
 * - Done: món đã xong, khách sẽ nhận thông báo.
 */
@Composable
fun KitchenDisplayScreen(
    viewModel: PosViewModel,
    onNotificationClick: (NotificationItem) -> Unit = {}
) {
    val orders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Cần làm", "Đang nấu", "Hoàn tất")

    LaunchedEffect(Unit) {
        viewModel.newOrderEvent.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Điều phối Nhà Bếp",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StaffNotificationBell(
                            notifications = notifications,
                            unreadCount = unreadCount,
                            onOpen = { viewModel.markAllRead() },
                            onClear = { viewModel.clearNotifications() },
                            onNotificationClick = onNotificationClick
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .height(54.dp)
                            .background(Color(0xFFF1F1F1), RoundedCornerShape(27.dp))
                            .padding(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            tabs.forEachIndexed { index, title ->
                                val count = when(index) {
                                    0 -> orders.sumOf { o -> o.items.count { it.status == "Pending" } }
                                    1 -> orders.sumOf { o -> o.items.count { it.status == "Cooking" } }
                                    else -> orders.sumOf { o -> o.items.count { it.status == "Done" } }
                                }
                                
                                val isSelected = selectedTab == index
                                val tabColor = when(index) {
                                    0 -> ActionRed
                                    1 -> BrandYellow
                                    else -> ActionGreen
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(23.dp))
                                        .background(if (isSelected) tabColor else Color.Transparent)
                                        .clickable { selectedTab = index },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = title,
                                            color = if (isSelected) (if (index == 1) Color.Black else Color.White) else Color.Gray,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (count > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.1f),
                                                shape = CircleShape
                                            ) {
                                                Text(
                                                    text = "$count",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSelected) (if (index == 1) Color.Black else Color.White) else Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        val currentStatus = when(selectedTab) {
            0 -> "Pending"
            1 -> "Cooking"
            else -> "Done"
        }
        
        val filteredItems = remember(orders, currentStatus) {
            // OrderItem hiện không có id riêng, nên KDS lưu cả orderId và index trong list items.
            // Khi bấm đổi trạng thái, ViewModel dùng cặp này để sửa đúng phần tử trong order gốc.
            val list = mutableListOf<Triple<String, Int, OrderItem>>()
            orders.forEach { order ->
                order.items.forEachIndexed { index, item ->
                    if (item.status == currentStatus) {
                        list.add(Triple(order.id, index, item))
                    }
                }
            }
            list
        }

        if (filteredItems.isEmpty()) {
            EmptyState(
                icon = Icons.Default.RestaurantMenu,
                title = "Đang trống",
                description = "Chưa có món nào ở mục ${tabs[selectedTab]}",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(filteredItems, key = { _, item -> "${item.first}_${item.second}_${item.third.menuItemId}" }) { _, (orderId, index, item) ->
                    val order = orders.find { it.id == orderId }
                    val menuItem = menuItems.find { it.id == item.menuItemId }
                    
                    KitchenItemCard(
                        itemName = item.name,
                        imageUrl = menuItem?.imageUrl ?: "",
                        quantity = item.quantity,
                        tableId = order?.tableId ?: "?",
                        status = item.status,
                        onAction = {
                            // Pipeline bếp:
                            // Pending -> Cooking: bếp nhận món và bắt đầu làm.
                            // Cooking -> Done: món sẵn sàng; ViewModel gửi FCM về bàn của khách.
                            val nextStatus = if (item.status == "Pending") "Cooking" else "Done"
                            viewModel.updateOrderItemStatus(orderId, index, nextStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KitchenItemCard(
    itemName: String,
    imageUrl: String,
    quantity: Int,
    tableId: String,
    status: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncFoodImage(
                    imageUrl = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itemName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF212121),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TableBar, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bàn $tableId", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
                
                Surface(
                    color = BrandYellow.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "x$quantity",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandYellow
                    )
                }
            }

            if (status != "Done") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "Pending") ActionRed else BrandYellow
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (status == "Pending") "BẮT ĐẦU CHẾ BIẾN" else "HOÀN TẤT MÓN",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (status == "Pending") Color.White else Color.Black
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = ActionGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Món ăn đã sẵn sàng phục vụ", fontSize = 12.sp, color = ActionGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
