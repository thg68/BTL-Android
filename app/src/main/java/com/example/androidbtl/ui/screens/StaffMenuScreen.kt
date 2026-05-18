package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun StaffMenuScreen(
    viewModel: PosViewModel,
    onShowMessage: (String) -> Unit = {}
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val isLoading by viewModel.isLoadingMenu.collectAsState()

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
                    description = "Dữ liệu mẫu đang được tự khởi tạo"
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(menuItems) { item ->
                        StaffMenuItemCard(item) { isAvailable ->
                            viewModel.updateMenuItemAvailability(item.id, isAvailable)
                            val msg = if (isAvailable) "Đã hiện món ${item.name}" else "Đã ẩn món ${item.name}"
                            onShowMessage(msg)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
fun StaffMenuItemCard(item: MenuItem, onAvailabilityChange: (Boolean) -> Unit) {
    val mutedAlpha = if (item.isAvailable) 1f else 0.45f
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
