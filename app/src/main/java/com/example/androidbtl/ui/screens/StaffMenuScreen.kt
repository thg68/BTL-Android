package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.ui.components.AsyncFoodImage
import com.example.androidbtl.ui.components.EmptyState
import com.example.androidbtl.ui.components.MenuItemSkeleton
import com.example.androidbtl.ui.theme.ActionGreen
import com.example.androidbtl.ui.theme.ActionRed
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel

private val menuCategories = listOf(
    "Thịt bò", "Thịt lợn", "Hải sản", "Rau nấm", "Ăn kèm", "Tráng miệng", "Nước lẩu"
)

@Composable
fun StaffMenuScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.menuItems.collectAsState()
    val isLoading by viewModel.isLoadingMenu.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var deletingItem by remember { mutableStateOf<MenuItem?>(null) }

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
                    Text(
                        "Quản lý Món ăn",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }

            when {
                isLoading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(6) { MenuItemSkeleton() }
                    }
                }
                menuItems.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Inventory,
                        title = "Chưa có món ăn nào",
                        description = "Nhấn + để thêm món mới"
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(menuItems, key = { it.id }) { item ->
                            StaffMenuItemCard(
                                item = item,
                                onAvailabilityChange = { viewModel.updateMenuItemAvailability(item.id, it) },
                                onEdit = { editingItem = item },
                                onDelete = { deletingItem = item }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
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
            Icon(Icons.Filled.Add, contentDescription = "Thêm món")
        }
    }

    if (showAddDialog || editingItem != null) {
        MenuItemDialog(
            item = editingItem,
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onConfirm = { name, cat, price, desc, imgUrl ->
                val current = editingItem
                if (current != null) {
                    viewModel.updateMenuItem(
                        current.copy(name = name, category = cat, price = price, description = desc, imageUrl = imgUrl)
                    )
                } else {
                    viewModel.addMenuItem(name, cat, price, desc, imgUrl)
                }
                showAddDialog = false
                editingItem = null
            }
        )
    }

    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Xóa món ăn", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa \"${item.name}\"?\nHành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteMenuItem(item.id); deletingItem = null }) {
                    Text("Xóa", color = ActionRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) { Text("Hủy") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDialog(
    item: MenuItem?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, price: Double, description: String, imageUrl: String) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: menuCategories.first()) }
    var price by remember { mutableStateOf(if (item != null) item.price.toLong().toString() else "") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var imageUrl by remember { mutableStateOf(item?.imageUrl ?: "") }
    var expandedCategory by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (item == null) "Thêm món mới" else "Sửa món ăn",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Tên món *") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("Vui lòng nhập tên món") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        menuCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { category = cat; expandedCategory = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; priceError = false },
                    label = { Text("Giá (VNĐ) *") },
                    isError = priceError,
                    supportingText = if (priceError) {{ Text("Vui lòng nhập giá hợp lệ") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL ảnh") },
                    placeholder = { Text("Để trống sẽ tự tìm ảnh phù hợp", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = price.toDoubleOrNull()
                    nameError = name.isBlank()
                    priceError = priceVal == null || priceVal <= 0
                    if (!nameError && !priceError) {
                        onConfirm(name.trim(), category, priceVal!!, description.trim(), imageUrl.trim())
                    }
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
fun StaffMenuItemCard(
    item: MenuItem,
    onAvailabilityChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val mutedAlpha = if (item.isAvailable) 1f else 0.45f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncFoodImage(
                imageUrl = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = mutedAlpha)
                )
                Text(
                    item.category,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${"%,.0f".format(item.price)}đ",
                    color = BrandYellow.copy(alpha = mutedAlpha),
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Sửa",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Xóa",
                            tint = ActionRed,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = item.isAvailable,
                    onCheckedChange = { onAvailabilityChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BrandYellow,
                        checkedTrackColor = BrandYellow.copy(alpha = 0.5f)
                    )
                )
                Text(
                    text = if (item.isAvailable) "Đang bán" else "Hết hàng",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isAvailable) ActionGreen else ActionRed
                )
            }
        }
    }
}
