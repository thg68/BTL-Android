package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
        // Distinct Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column {
                Text(
                    "Điều phối Nhà Bếp",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    color = TextPrimary
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        }

        // Column Titles
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

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pending Column
            KdsColumn(
                orders = orders,
                targetStatus = "Pending",
                modifier = Modifier.weight(1f)
            ) { orderId, index ->
                viewModel.updateOrderItemStatus(orderId, index, "Cooking")
            }
            
            // Cooking Column
            KdsColumn(
                orders = orders,
                targetStatus = "Cooking",
                modifier = Modifier.weight(1f)
            ) { orderId, index ->
                viewModel.updateOrderItemStatus(orderId, index, "Done")
            }

            // Done Column
            KdsColumn(
                orders = orders,
                targetStatus = "Done",
                modifier = Modifier.weight(1f)
            ) { _, _ -> }
        }
    }
}

@Composable
fun KdsHeader(title: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.1f),
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

val ActionRed = Color(0xFFE53935)

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
            itemsIndexed(order.items) { index, item ->
                if (item.status == targetStatus) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Surface(
                                    color = BrandYellow.copy(alpha = 0.1f),
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
                            Text("Bàn: ${order.tableId}", color = Color.Gray, fontSize = 11.sp)
                            
                            if (targetStatus != "Done") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onItemClick(order.id, index) },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
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
