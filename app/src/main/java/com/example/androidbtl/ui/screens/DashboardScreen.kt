package com.example.androidbtl.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.ui.theme.ActionGreen
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: PosViewModel) {
    val closedOrders by viewModel.closedOrders.collectAsState()
    val tables by viewModel.tables.collectAsState()

    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayOrders = closedOrders.filter { it.timestamp >= startOfToday }
    val todayRevenue = todayOrders.sumOf { it.totalAmount }
    val activeTables = tables.count { it.status == "Đang phục vụ" }

    val topDishes = closedOrders
        .flatMap { it.items }
        .groupBy { it.name }
        .map { (name, items) -> name to items.sumOf { it.quantity } }
        .sortedByDescending { it.second }
        .take(5)

    val last7Days = remember(closedOrders) { computeLast7DaysRevenue(closedOrders) }

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
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = BrandYellow,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Báo cáo doanh thu",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatCardsRow(
                    revenue = todayRevenue,
                    orderCount = todayOrders.size,
                    activeTables = activeTables
                )
            }
            item { RevenueChartCard(last7Days) }
            item { TopDishesCard(topDishes) }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun StatCardsRow(revenue: Double, orderCount: Int, activeTables: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.AttachMoney,
            iconTint = BrandYellow,
            label = "Doanh thu hôm nay",
            value = formatShortMoney(revenue)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.ReceiptLong,
            iconTint = ActionGreen,
            label = "Đơn đã đóng",
            value = orderCount.toString()
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.TableRestaurant,
            iconTint = Color(0xFF1976D2),
            label = "Bàn đang phục vụ",
            value = activeTables.toString()
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                color = iconTint.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.padding(6.dp).size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun RevenueChartCard(data: List<Pair<String, Double>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Doanh thu 7 ngày gần nhất",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))
            BarChart(data = data)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { (label, _) ->
                    Text(
                        label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChart(data: List<Pair<String, Double>>) {
    val maxValue = (data.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    val barColor = BrandYellow
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val density = LocalDensity.current

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val gapPx = with(density) { 8.dp.toPx() }
        val corner = with(density) { 6.dp.toPx() }
        val chartH = size.height - 8f
        val slotWidth = size.width / data.size

        // baseline
        drawLine(
            color = gridColor,
            start = Offset(0f, chartH),
            end = Offset(size.width, chartH),
            strokeWidth = with(density) { 1.dp.toPx() }
        )
        // 3 horizontal grid lines
        for (i in 1..3) {
            val y = chartH - chartH * i / 4f
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = with(density) { 0.5.dp.toPx() }
            )
        }

        data.forEachIndexed { i, (_, value) ->
            val ratio = (value / maxValue).toFloat().coerceIn(0f, 1f)
            val h = chartH * ratio
            val barW = slotWidth - gapPx
            val x = i * slotWidth + gapPx / 2f
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, chartH - h),
                size = Size(barW, h),
                cornerRadius = CornerRadius(corner, corner)
            )
        }
    }
}

@Composable
private fun TopDishesCard(dishes: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Top 5 món bán chạy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (dishes.isEmpty()) {
                Text(
                    "Chưa có dữ liệu",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxQty = dishes.maxOf { it.second }
                dishes.forEachIndexed { idx, (name, qty) ->
                    TopDishRow(rank = idx + 1, name = name, quantity = qty, maxQty = maxQty)
                    if (idx != dishes.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopDishRow(rank: Int, name: String, quantity: Int, maxQty: Int) {
    val ratio = if (maxQty == 0) 0f else quantity.toFloat() / maxQty
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "$rank. $name",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "$quantity phần",
                color = BrandYellow,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    RoundedCornerShape(6.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(6.dp)
                    .background(BrandYellow, RoundedCornerShape(6.dp))
            )
        }
    }
}

private fun computeLast7DaysRevenue(
    orders: List<com.example.androidbtl.data.models.Order>
): List<Pair<String, Double>> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val day = 24L * 60 * 60 * 1000
    val format = SimpleDateFormat("dd/MM", Locale("vi"))

    val today = cal.timeInMillis
    val buckets = (6 downTo 0).map { offset ->
        val start = today - offset * day
        val end = start + day
        val label = format.format(java.util.Date(start))
        val sum = orders.filter { it.timestamp in start until end }.sumOf { it.totalAmount }
        label to sum
    }
    return buckets
}

private fun formatShortMoney(amount: Double): String {
    return when {
        amount >= 1_000_000 -> "%.1ftr".format(amount / 1_000_000.0)
        amount >= 1_000 -> "%.0fk".format(amount / 1_000.0)
        else -> "%.0fđ".format(amount)
    }
}
