package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary
import com.example.androidbtl.viewmodel.PosViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

/**
 * Báo cáo doanh thu trong ngày từ các order đã đóng.
 */
@Composable
fun RevenueScreen(viewModel: PosViewModel) {
    val closedOrders by viewModel.closedOrders.collectAsStateWithLifecycle()
    
    // Lọc đơn hàng chỉ trong ngày hôm nay
    val todayOrders = remember(closedOrders) {
        // Mốc hôm nay lấy theo timezone thiết bị để lọc giao dịch trong ngày.
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis
        
        closedOrders.filter { it.timestamp >= startOfToday }
    }
    
    val totalRevenue = todayOrders.sumOf { it.totalAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column {
                Text(
                    "Báo cáo Doanh thu",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    color = TextPrimary
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        }

        // Revenue Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Tổng doanh thu hôm nay", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${"%,.0f".format(totalRevenue)}đ",
                    color = BrandYellow,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tổng cộng ${todayOrders.size} đơn hàng đã hoàn tất",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }

        Text(
            "Lịch sử giao dịch hôm nay",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        if (todayOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có giao dịch nào hoàn tất trong hôm nay", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(todayOrders, key = { it.id }) { order ->
                    TransactionRow(order = order)
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun TransactionRow(order: com.example.androidbtl.data.models.Order) {
    val date = Date(order.timestamp)
    val format = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Bàn ${order.tableId}", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(format.format(date), fontSize = 11.sp, color = Color.Gray)
            }
            Text(
                "${"%,.0f".format(order.totalAmount)}đ",
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                fontSize = 16.sp
            )
        }
    }
}
