package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbtl.data.models.OrderItem
import com.example.androidbtl.ui.components.EmptyState
import com.example.androidbtl.ui.components.QrPaymentDialog
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel

/**
 * Màn hóa đơn của khách.
 *
 * Khách có thể:
 * - Xem các món đã gửi bếp hoặc đã hoàn tất.
 * - Tạo QR VietQR theo tổng tiền hiện tại.
 * - Bấm báo đã thanh toán để nhân viên thấy hóa đơn ở tab xác nhận.
 *
 * Màn này không tự đóng order và không tự logout khách. Việc xác nhận tiền/đóng bàn
 * là trách nhiệm của nhân viên ở BillingScreen và TableManagementScreen.
 */
@Composable
fun BillScreen(
        tableId: String,
        viewModel: PosViewModel,
        onShowMessage: (String) -> Unit = {}
) {
    val orders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val tableOrders = remember(orders, tableId) { orders.filter { it.tableId == tableId } }

    val mergedItems =
            remember(tableOrders) {
                // Không tính món còn trong Cart vì đó là món khách mới chọn nhưng chưa gửi bếp.
                // Các dòng cùng menuItemId được gộp lại để hóa đơn dễ đọc, ví dụ cùng một món
                // được gửi nhiều lần sẽ chỉ hiện một dòng với tổng quantity.
                tableOrders
                        .flatMap { it.items }
                        .filter { it.status == "Pending" || it.status == "Done" }
                        .groupBy { it.menuItemId }
                        .map { (_, items) ->
                            val first = items.first()
                            first.copy(quantity = items.sumOf { it.quantity })
                        }
                        .sortedBy { it.name }
            }
    val totalAmount = remember(mergedItems) { mergedItems.sumOf { it.price * it.quantity } }

    var showQrDialog by remember { mutableStateOf(false) }
    var hasNotifiedPayment by rememberSaveable(tableId) { mutableStateOf(false) }

    Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (mergedItems.isNotEmpty()) {
                    Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 16.dp,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp).navigationBarsPadding()) {
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                        "Tổng thanh toán",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                )
                                Text(
                                        "${"%,.0f".format(totalAmount)}đ",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandYellow
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                    onClick = { showQrDialog = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    containerColor = BrandYellow
                                            ),
                                    shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                        Icons.Filled.QrCode2,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        "TẠO QR THANH TOÁN",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                    onClick = {
                                        // Đây chỉ là tín hiệu "khách đã chuyển khoản".
                                        // ViewModel sẽ tạo notification cho staff nhưng không đóng order/bàn.
                                        // Nút bị khóa sau lần đầu để tránh spam cùng một hóa đơn.
                                        viewModel.notifyPaymentSuccess(tableId, totalAmount)
                                        hasNotifiedPayment = true
                                        onShowMessage(
                                                "Đã thông báo cho nhân viên kiểm tra thanh toán."
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    enabled = !hasNotifiedPayment,
                                    shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                        Icons.Filled.Payments,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        if (hasNotifiedPayment) "ĐÃ BÁO NHÂN VIÊN"
                                        else "TÔI ĐÃ THANH TOÁN",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
    ) { padding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(padding)
                                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
            ) {
                Column(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = BrandYellow,
                            modifier = Modifier.size(28.dp)
                    )
                    Text(
                            "Hóa đơn Bàn $tableId",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }

            if (mergedItems.isEmpty()) {
                EmptyState(
                        icon = Icons.Filled.Receipt,
                        title = "Chưa có món ăn nào",
                        description = "Hoá đơn sẽ hiện khi món được gửi xuống bếp"
                )
            } else {
                LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { BillOrderCard(title = "Chi tiết các món", items = mergedItems) }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }

    if (showQrDialog) {
        QrPaymentDialog(
                amount = totalAmount,
                tableId = tableId,
                onDismiss = { showQrDialog = false }
        )
    }
}

@Composable
fun BillOrderCard(title: String, items: List<OrderItem>) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            items.forEach { item ->
                BillItemRow(item = item)
                if (item != items.last()) {
                    HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                    "x${item.quantity} × ${"%,.0f".format(item.price)}đ",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
                "${"%,.0f".format(item.price * item.quantity)}đ",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandYellow
        )
    }
}
