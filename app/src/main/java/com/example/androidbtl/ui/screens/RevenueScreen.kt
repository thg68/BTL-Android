package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.Order
import com.example.androidbtl.ui.components.StaffNotificationBell
import com.example.androidbtl.ui.theme.ActionGreen
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.*

private enum class RevenueFilter(val label: String) {
    TODAY("Hôm nay"),
    WEEK("7 ngày"),
    MONTH("30 ngày"),
    ALL("Tất cả")
}

@Composable
fun RevenueScreen(viewModel: PosViewModel) {
    val closedOrders by viewModel.closedOrders.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    var selectedFilter by remember { mutableStateOf(RevenueFilter.TODAY) }

    val filtered = remember(closedOrders, selectedFilter) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        when (selectedFilter) {
            RevenueFilter.TODAY -> closedOrders.filter { it.timestamp >= startOfDay }
            RevenueFilter.WEEK -> closedOrders.filter { it.timestamp >= now - 7 * 24 * 3600 * 1000L }
            RevenueFilter.MONTH -> closedOrders.filter { it.timestamp >= now - 30 * 24 * 3600 * 1000L }
            RevenueFilter.ALL -> closedOrders
        }
    }

    val totalRevenue = filtered.sumOf { it.totalAmount }
    val orderCount = filtered.size
    val avgRevenue = if (orderCount > 0) totalRevenue / orderCount else 0.0

    val revenueByTable = filtered
        .groupBy { it.tableId }
        .map { (tableId, orders) -> tableId to orders.sumOf { it.totalAmount } }
        .sortedByDescending { it.second }
        .take(5)

    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Báo cáo Doanh thu",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RevenueFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandYellow,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.TrendingUp,
                    iconTint = ActionGreen,
                    label = "Doanh thu",
                    value = formatMoney(totalRevenue)
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Receipt,
                    iconTint = BrandYellow,
                    label = "Số đơn",
                    value = orderCount.toString()
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.BarChart,
                    iconTint = MaterialTheme.colorScheme.primary,
                    label = "TB/đơn",
                    value = formatMoney(avgRevenue)
                )
            }

            // Top tables
            if (revenueByTable.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.TableRestaurant,
                                contentDescription = null,
                                tint = BrandYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Doanh thu theo bàn",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val maxRevenue = revenueByTable.first().second
                        revenueByTable.forEachIndexed { index, (tableId, revenue) ->
                            TableRevenueRow(
                                rank = index + 1,
                                tableId = tableId,
                                revenue = revenue,
                                maxRevenue = maxRevenue
                            )
                            if (index < revenueByTable.lastIndex) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }

            // Order list
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Chưa có doanh thu trong khoảng thời gian này",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Chi tiết đơn hàng (${filtered.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        filtered.forEachIndexed { index, order ->
                            OrderRevenueRow(order = order, dateFormat = dateFormat)
                            if (index < filtered.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Text(
                value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TableRevenueRow(
    rank: Int,
    tableId: String,
    revenue: Double,
    maxRevenue: Double
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val progress = if (maxRevenue > 0) (revenue / maxRevenue).toFloat() else 0f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "#$rank",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = rankColor,
                modifier = Modifier.width(28.dp)
            )
            Text(
                "Bàn $tableId",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                formatMoney(revenue),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ActionGreen
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .padding(start = 38.dp),
            color = BrandYellow,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun OrderRevenueRow(order: Order, dateFormat: SimpleDateFormat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Bàn ${order.tableId}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${order.items.filter { it.status != "Cart" }.sumOf { it.quantity }} món  •  ${dateFormat.format(Date(order.timestamp))}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            formatMoney(order.totalAmount),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = BrandYellow
        )
    }
}

private fun formatMoney(amount: Double): String {
    return if (amount >= 1_000_000) {
        "${"%.1f".format(amount / 1_000_000)}tr"
    } else {
        "${"%,.0f".format(amount)}đ"
    }
}
