package com.example.androidbtl.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbtl.data.models.Order
import com.example.androidbtl.data.models.NotificationItem
import com.example.androidbtl.data.models.RestaurantTable
import com.example.androidbtl.ui.components.QrPaymentDialog
import com.example.androidbtl.ui.components.StaffNotificationBell
import com.example.androidbtl.ui.theme.ActionRed
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel
import qrcode.QRCode

private const val TABLE_LINK_SCHEME = "androidbtl://table"

/**
 * Tab quản lý bàn: mở/đóng phiên bàn, đặt bàn, QR đăng nhập, QR thanh toán và CRUD bàn.
 */
@Composable
fun TableManagementScreen(
    viewModel: PosViewModel,
    onLogout: () -> Unit = {},
    onNotificationClick: (NotificationItem) -> Unit = {}
) {
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingTables.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<RestaurantTable?>(null) }
    var optionsTable by remember { mutableStateOf<RestaurantTable?>(null) }
    var selectedTable by remember { mutableStateOf<RestaurantTable?>(null) }
    var deletingTable by remember { mutableStateOf<RestaurantTable?>(null) }
    var accessQrTable by remember { mutableStateOf<RestaurantTable?>(null) }
    var paymentRequest by remember { mutableStateOf<Pair<String, Double>?>(null) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi hệ thống?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Đăng xuất", color = ActionRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            }
        )
    }

    if (showAddDialog || editingTable != null) {
        TableDialog(
            table = editingTable,
            onDismiss = { showAddDialog = false; editingTable = null },
            onConfirm = { name, capacity ->
                val table = editingTable
                if (table != null) viewModel.updateTable(table.id, name, capacity)
                else viewModel.addTable(name, capacity)
                showAddDialog = false
                editingTable = null
            }
        )
    }

    selectedTable?.let { table ->
        val activeOrder = activeOrders.find { it.tableId == table.id }
        TableSettingsDialog(
            table = table,
            activeOrder = activeOrder,
            onDismiss = { selectedTable = null },
            onOpenTableQr = {
                // Bàn đang phục vụ dùng lại accessCode hiện tại để không ghi đè phiên khách.
                val accessCode =
                    if (table.status == "Đang phục vụ" && table.accessCode.isNotBlank()) {
                        table.accessCode
                    } else {
                        viewModel.openTableForCustomer(table.id)
                    }
                selectedTable = null
                accessQrTable = table.copy(status = "Đang phục vụ", accessCode = accessCode)
            },
            onReserveTable = {
                if (table.status == "Đã đặt") viewModel.cancelTableReservation(table.id)
                else viewModel.reserveTable(table.id)
                selectedTable = null
            },
            onCloseTable = {
                // Đóng phiên bàn sẽ xóa QR/token và làm khách ở bàn đó logout.
                viewModel.closeTable(table.id)
                selectedTable = null
            },
            onPaymentQr = { amount ->
                // QR thanh toán không đóng order; nhân viên vẫn phải xác nhận ở BillingScreen.
                selectedTable = null
                paymentRequest = table.id to amount
            }
        )
    }

    accessQrTable?.let { table ->
        TableAccessQrDialog(
            table = table,
            onDismiss = { accessQrTable = null }
        )
    }

    paymentRequest?.let { (tableId, amount) ->
        QrPaymentDialog(
            amount = amount,
            tableId = tableId,
            onDismiss = { paymentRequest = null }
        )
    }

    optionsTable?.let { table ->
        AlertDialog(
            onDismissRequest = { optionsTable = null },
            title = { Text(table.name, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Sức chứa: ${table.capacity} người  •  ${table.status}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { editingTable = table; optionsTable = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sửa thông tin bàn")
                    }
                    Button(
                        onClick = { deletingTable = table; optionsTable = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ActionRed),
                        enabled = table.status == "Trống"
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (table.status == "Trống") "Xóa bàn" else "Không thể xóa")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { optionsTable = null }) { Text("Đóng") }
            }
        )
    }

    deletingTable?.let { table ->
        AlertDialog(
            onDismissRequest = { deletingTable = null },
            title = { Text("Xóa bàn", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa \"${table.name}\"?\nHành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTable(table.id); deletingTable = null }) {
                    Text("Xóa", color = ActionRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTable = null }) { Text("Hủy") }
            }
        )
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
                shadowElevation = 1.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Sơ đồ bàn",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${tables.size} bàn đang quản lý",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StaffNotificationBell(
                                notifications = notifications,
                                unreadCount = unreadCount,
                                onOpen = { viewModel.markAllRead() },
                                onClear = { viewModel.clearNotifications() },
                                onNotificationClick = onNotificationClick
                            )
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Đăng xuất",
                                    tint = ActionRed
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TableStatusSummaryCard(
                    label = "Trống",
                    count = tables.count { it.status == "Trống" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TableStatusSummaryCard(
                    label = "Phục vụ",
                    count = tables.count { it.status == "Đang phục vụ" },
                    color = BrandYellow,
                    modifier = Modifier.weight(1f)
                )
                TableStatusSummaryCard(
                    label = "Đã đặt",
                    count = tables.count { it.status == "Đã đặt" },
                    color = ActionRed,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandYellow)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 96.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tables, key = { it.id }) { table ->
                        TableCard(
                            table = table,
                            onClick = { selectedTable = table },
                            onLongClick = { optionsTable = table }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 88.dp),
            containerColor = BrandYellow,
            contentColor = Color.Black
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Thêm bàn")
        }
    }
}

@Composable
private fun TableSettingsDialog(
    table: RestaurantTable,
    activeOrder: Order?,
    onDismiss: () -> Unit,
    onOpenTableQr: () -> Unit,
    onReserveTable: () -> Unit,
    onCloseTable: () -> Unit,
    onPaymentQr: (Double) -> Unit
) {
    // Chưa có món/order thì không cho tạo QR thanh toán.
    val payableAmount = activeOrder?.items.orEmpty().sumOf { it.price * it.quantity }
    // QR đăng nhập vẫn hiện khi bàn đang phục vụ để nhân viên mở lại mã cho khách cùng bàn.
    val canReserve = table.status != "Đang phục vụ"
    val canCloseTable = table.status == "Đang phục vụ"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(table.name, fontWeight = FontWeight.ExtraBold)
                Text(table.status, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onReserveTable,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    enabled = canReserve
                ) {
                    Icon(Icons.Filled.TableRestaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when {
                            table.status == "Đã đặt" -> "Hủy đặt bàn"
                            canReserve -> "Đặt bàn"
                            else -> "Bàn đang phục vụ"
                        }
                    )
                }
                Button(
                    onClick = onOpenTableQr,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandYellow)
                ) {
                    Icon(Icons.Filled.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (table.status == "Đang phục vụ") "QR đăng nhập"
                        else "Mở bàn + QR đăng nhập",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = { onPaymentQr(payableAmount) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    enabled = payableAmount > 0.0
                ) {
                    Icon(Icons.Filled.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (payableAmount > 0.0) "QR thanh toán (${formatMoney(payableAmount)})"
                        else "Chưa có món để thanh toán"
                    )
                }
                Text(
                    "Nhấn giữ thẻ bàn để sửa hoặc xóa bàn.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (canCloseTable) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Button(
                        onClick = onCloseTable,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ActionRed)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đóng bàn", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
private fun TableAccessQrDialog(
    table: RestaurantTable,
    onDismiss: () -> Unit
) {
    // Deep link chứa accessCode phiên bàn để khách quét QR vào đúng bàn đang mở.
    val payload = "$TABLE_LINK_SCHEME/${table.id}?code=${table.accessCode}"
    val qrBitmap: ImageBitmap = remember(payload) {
        val bytes = QRCode.ofSquares()
            .withSize(12)
            .build(payload)
            .renderToBytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.QrCode2, contentDescription = null, tint = BrandYellow, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("QR mở ${table.name}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Khách quét mã này để tự động đăng nhập vào bàn.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Image(bitmap = qrBitmap, contentDescription = "QR mở bàn", modifier = Modifier.size(240.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(payload, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Đóng", color = BrandYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TableDialog(
    table: RestaurantTable?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, capacity: Int) -> Unit
) {
    var name by remember { mutableStateOf(table?.name ?: "") }
    var capacity by remember { mutableStateOf(table?.capacity?.toString() ?: "4") }
    var nameError by remember { mutableStateOf(false) }
    var capacityError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (table == null) "Thêm bàn mới" else "Sửa thông tin bàn",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Tên bàn *") },
                    placeholder = { Text("Ví dụ: Bàn VIP 1") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Vui lòng nhập tên bàn") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it; capacityError = false },
                    label = { Text("Sức chứa (người) *") },
                    isError = capacityError,
                    supportingText = if (capacityError) {{ Text("Vui lòng nhập số người hợp lệ") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacity.toIntOrNull()
                    nameError = name.isBlank()
                    capacityError = cap == null || cap <= 0
                    if (!nameError && !capacityError) onConfirm(name.trim(), cap!!)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandYellow)
            ) {
                Text("Lưu", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
private fun TableStatusSummaryCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(64.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(
                count.toString(),
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCard(
    table: RestaurantTable,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val accentColor = when (table.status) {
        "Đang phục vụ" -> BrandYellow
        "Đã đặt" -> ActionRed
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusContainer = when (table.status) {
        "Đang phục vụ" -> BrandYellow
        "Đã đặt" -> ActionRed
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val statusContent = when (table.status) {
        "Đang phục vụ" -> Color.Black
        "Đã đặt" -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .height(118.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.13f), RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = accentColor.copy(alpha = if (table.status == "Trống") 0.10f else 0.18f),
                shape = CircleShape,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.TableRestaurant,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    table.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
                Surface(
                    color = statusContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 5.dp)
                ) {
                    Text(
                        table.status,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        color = statusContent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Text(
                "${table.capacity} người",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatMoney(amount: Double): String = "%,.0fđ".format(amount)
