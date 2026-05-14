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
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun StaffMenuScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.menuItems.collectAsState()

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
                    "Quản lý Món ăn",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    color = TextPrimary
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(menuItems) { item ->
                StaffMenuItemCard(item) { isAvailable ->
                    viewModel.updateMenuItemAvailability(item.id, isAvailable)
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun StaffMenuItemCard(item: MenuItem, onAvailabilityChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (item.isAvailable) TextPrimary else Color.Gray
                )
                Text(
                    item.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    "${"%,.0f".format(item.price)}đ",
                    color = if (item.isAvailable) BrandYellow else Color.Gray,
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
                    color = if (item.isAvailable) Color(0xFF4CAF50) else Color.Red
                )
            }
        }
    }
}
