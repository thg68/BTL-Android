package com.example.androidbtl.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.ui.components.EmptyState
import com.example.androidbtl.ui.components.StaffNotificationBell
import com.example.androidbtl.ui.theme.ActionGreen
import com.example.androidbtl.ui.theme.ActionRed
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun KitchenDisplayScreen(viewModel: PosViewModel) {
    val orders by viewModel.activeOrders.collectAsState()
    val hasAnyItem = orders.any { it.items.any { i -> i.status in listOf("Pending", "Cooking", "Done") } }
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.newOrderEvent.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Điều phối Nhà Bếp",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StaffNotificationBell(
                            notifications = notifications,
                            unreadCount = unreadCount,
                            onOpen = { viewModel.markAllRead() },
                            onClear = { viewModel.clearNotifications() }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KdsHeader(title = "CẦN LÀM", color = ActionRed, modifier = Modifier.weight(1f))
            KdsHeader(title = "ĐANG NẤU", color = BrandYellow, modifier = Modifier.weight(1f))
            KdsHeader(title = "HOÀN TẤT", color = ActionGreen, modifier = Modifier.weight(1f))
        }

        if (!hasAnyItem) {
            EmptyState(
                icon = Icons.Filled.RestaurantMenu,
                title = "Chưa có món nào cần xử lý",
                description = "Khi khách gửi món sẽ tự xuất hiện ở đây"
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KdsColumn(orders = orders, targetStatus = "Pending", modifier = Modifier.weight(1f)) { orderId, index ->
                    viewModel.updateOrderItemStatus(orderId, index, "Cooking")
                }
                KdsColumn(orders = orders, targetStatus = "Cooking", modifier = Modifier.weight(1f)) { orderId, index ->
                    viewModel.updateOrderItemStatus(orderId, index, "Done")
                }
                KdsColumn(orders = orders, targetStatus = "Done", modifier = Modifier.weight(1f)) { _, _ -> }
            }
        }
        } // end Column

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = ActionRed,
                contentColor = Color.White
            )
        }
    } // end Box
}

@Composable
fun KdsHeader(title: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = color
        )
    }
}

@Composable
fun KdsColumn(
    orders: List<com.example.androidbtl.data.models.Order>,
    targetStatus: String,
    modifier: Modifier = Modifier,
    onItemClick: (String, Int) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        orders.forEach { order ->
            itemsIndexed(order.items, key = { idx, _ -> "${order.id}_$idx" }) { index, item ->
                AnimatedVisibility(
                    visible = item.status == targetStatus,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    color = BrandYellow.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "x${item.quantity}",
                                        color = BrandYellow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "Bàn: ${order.tableId}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )

                            if (targetStatus != "Done") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onItemClick(order.id, index) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (targetStatus == "Pending") ActionRed else BrandYellow
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        if (targetStatus == "Pending") "CHẾ BIẾN" else "HOÀN TẤT",
                                        color = if (targetStatus == "Pending") Color.White else Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
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
