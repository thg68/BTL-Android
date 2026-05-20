package com.example.androidbtl.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.RestaurantTable
import com.example.androidbtl.ui.components.StaffNotificationBell
import com.example.androidbtl.ui.theme.ActionGreen
import com.example.androidbtl.ui.theme.ActionRed
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun TableManagementScreen(
    viewModel: PosViewModel,
    onTableClick: (String, String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val tables by viewModel.tables.collectAsState()
    val isLoading by viewModel.isLoadingTables.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTable by remember { mutableStateOf<RestaurantTable?>(null) }
    var optionsTable by remember { mutableStateOf<RestaurantTable?>(null) }
    var deletingTable by remember { mutableStateOf<RestaurantTable?>(null) }

    // Logout confirm
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

    // Add / Edit dialog
    if (showAddDialog || editingTable != null) {
        TableDialog(
            table = editingTable,
            onDismiss = { showAddDialog = false; editingTable = null },
            onConfirm = { name, capacity ->
                val t = editingTable
                if (t != null) viewModel.updateTable(t.id, name, capacity)
                else viewModel.addTable(name, capacity)
                showAddDialog = false
                editingTable = null
            }
        )
    }

    // Options dialog (long press)
    optionsTable?.let { table ->
        AlertDialog(
            onDismissRequest = { optionsTable = null },
            title = {
                Text(table.name, fontWeight = FontWeight.Bold)
            },
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
                        Text(
                            if (table.status == "Trống") "Xóa bàn" else "Không thể xóa (đang sử dụng)"
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { optionsTable = null }) { Text("Đóng") }
            }
        )
    }

    // Delete confirm
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
                            "Sơ đồ Bàn",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StaffNotificationBell(
                                notifications = notifications,
                                unreadCount = unreadCount,
                                onOpen = { viewModel.markAllRead() },
                                onClear = { viewModel.clearNotifications() }
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
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = MaterialTheme.colorScheme.surfaceVariant, text = "Trống")
                LegendItem(color = BrandYellow, text = "Đang phục vụ")
                LegendItem(color = ActionRed, text = "Đã đặt")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandYellow)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tables, key = { it.id }) { table ->
                        TableCard(
                            table = table,
                            onClick = { onTableClick(table.id, table.status) },
                            onLongClick = { optionsTable = table }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = BrandYellow,
            contentColor = Color.Black
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Thêm bàn")
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
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCard(
    table: RestaurantTable,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val backgroundColor = when (table.status) {
        "Đang phục vụ" -> BrandYellow
        "Đã đặt" -> ActionRed
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when (table.status) {
        "Đã đặt" -> Color.White
        "Đang phục vụ" -> Color.Black
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor =
        if (table.status == "Trống") MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color.Transparent

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    table.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = textColor,
                    maxLines = 2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = textColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        table.status,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (table.capacity > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${table.capacity} người",
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
