package com.example.androidbtl.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.OrderItem
import com.example.androidbtl.ui.components.AsyncFoodImage
import com.example.androidbtl.ui.components.EmptyState
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun BookingScreen(
    tableId: String,
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    val orders by viewModel.activeOrders.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()

    val activeOrder = orders.find { it.tableId == tableId && it.status == "Open" }
    val cartItems = activeOrder?.items?.filter { it.status == "Cart" } ?: emptyList()
    val totalAmount = cartItems.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Text(
                "Giỏ Hàng của bạn",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.weight(1f)) {
                EmptyState(
                    icon = Icons.Filled.ShoppingCart,
                    title = "Giỏ hàng trống",
                    description = "Hãy chọn món yêu thích để thêm vào giỏ!"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    val imageUrl = menuItems.find { it.id == item.menuItemId }?.imageUrl.orEmpty()
                    CartItemCard(
                        item = item,
                        imageUrl = imageUrl,
                        onDelete = {
                            activeOrder?.let { viewModel.removeOrderItem(it.id, item.menuItemId) }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Tổng cộng",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "${"%,.0f".format(totalAmount)}đ",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = BrandYellow
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        border = BorderStroke(1.dp, BrandYellow),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "TIẾP TỤC CHỌN",
                            color = BrandYellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            if (cartItems.isNotEmpty()) {
                                activeOrder?.let {
                                    viewModel.sendOrderToKitchen(it.id)
                                    onShowMessage("Đã gửi ${cartItems.sumOf { it.quantity }} món xuống bếp!")
                                }
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandYellow),
                        shape = RoundedCornerShape(12.dp),
                        enabled = cartItems.isNotEmpty()
                    ) {
                        Text(
                            "GỬI BẾP",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: OrderItem, imageUrl: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncFoodImage(
                imageUrl = imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${"%,.0f".format(item.price)}đ",
                    color = BrandYellow,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "x${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
