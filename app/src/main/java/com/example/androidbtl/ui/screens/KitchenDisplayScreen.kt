package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.ui.theme.ActionGreen
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun KitchenDisplayScreen(viewModel: PosViewModel) {
    val orders by viewModel.activeOrders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                "Màn hình nhà Bếp (KDS)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = TextPrimary
            )
        }

        // Horizontal layout for KDS typically
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Pending Column
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Cần làm", fontWeight = FontWeight.Bold, color = ActionRed)
                KdsColumn(orders, "Pending") { orderId, index ->
                    viewModel.updateOrderItemStatus(orderId, index, "Cooking")
                }
            }
            
            // Cooking Column
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text("Đang nấu", fontWeight = FontWeight.Bold, color = BrandYellow)
                KdsColumn(orders, "Cooking") { orderId, index ->
                    viewModel.updateOrderItemStatus(orderId, index, "Done")
                }
            }

            // Done Column
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text("Đã xong", fontWeight = FontWeight.Bold, color = ActionGreen)
                KdsColumn(orders, "Done") { _, _ -> /* Do nothing or clear */ }
            }
        }
    }
}

val ActionRed = Color(0xFFE53935)

@Composable
fun KdsColumn(orders: List<com.example.androidbtl.data.models.Order>, targetStatus: String, onItemClick: (String, Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        orders.forEach { order ->
            itemsIndexed(order.items) { index, item ->
                if (item.status == targetStatus) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("Bàn: ${order.tableId}", color = Color.Gray, fontSize = 12.sp)
                            Text("x${item.quantity}", color = BrandYellow, fontWeight = FontWeight.Bold)
                            
                            if (targetStatus != "Done") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onItemClick(order.id, index) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (targetStatus == "Pending") ActionRed else BrandYellow
                                    )
                                ) {
                                    Text(
                                        if (targetStatus == "Pending") "Xác nhận nấu" else "Nấu xong",
                                        color = if (targetStatus == "Pending") Color.White else Color.Black
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
