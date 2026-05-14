package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.Order
import com.example.androidbtl.data.models.OrderItem
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun BillScreen(tableId: String, viewModel: PosViewModel) {
    val orders by viewModel.activeOrders.collectAsState()

    // Gộp tất cả món đã gọi (Pending/Done) từ mọi đơn của bàn này
    val allBillItems = orders
        .filter { it.tableId == tableId }
        .flatMap { it.items }
        .filter { it.status == "Pending" || it.status == "Done" }

    val mergedItems = allBillItems
        .groupBy { it.menuItemId }
        .map { (_, items) ->
            val first = items.first()
            first.copy(quantity = items.sumOf { it.quantity })
        }
        .sortedBy { it.name }

    val totalAmount = mergedItems.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Restaurant, contentDescription = "Restaurant", tint = BrandYellow, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Hóa đơn Bàn $tableId",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        if (mergedItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Chưa có hóa đơn",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Bạn chưa gọi món nào",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BillOrderCard(
                        title = "Chi tiết hóa đơn",
                        items = mergedItems,
                        totalAmount = totalAmount
                    )
                }
            }
        }
    }
}

@Composable
fun BillOrderCard(
    title: String,
    items: List<OrderItem>,
    totalAmount: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "${"%,.0f".format(totalAmount)}đ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandYellow
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items list
            items.forEach { item ->
                BillItemRow(item = item)
                if (item != items.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun BillItemRow(item: OrderItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                "x${item.quantity} × ${"%,.0f".format(item.price)}đ",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            "${"%,.0f".format(item.price * item.quantity)}đ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BrandYellow
        )
    }
}
