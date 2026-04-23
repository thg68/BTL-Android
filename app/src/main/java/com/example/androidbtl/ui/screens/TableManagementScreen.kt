package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.androidbtl.data.models.RestaurantTable
import com.example.androidbtl.ui.theme.ActionRed
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun TableManagementScreen(viewModel: PosViewModel, onTableClick: (String, String) -> Unit) {
    val tables by viewModel.tables.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                "Sơ đồ Bàn",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem(color = Color.LightGray, text = "Trống")
            LegendItem(color = BrandYellow, text = "Đang phục vụ")
            LegendItem(color = ActionRed, text = "Đã đặt")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tables.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Đang tải dữ liệu bàn từ Firebase...")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tables) { table ->
                    TableCard(table = table) {
                        onTableClick(table.id, table.status)
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = TextPrimary)
    }
}

@Composable
fun TableCard(table: RestaurantTable, onClick: () -> Unit) {
    val backgroundColor = when (table.status) {
        "Đang phục vụ" -> BrandYellow
        "Đã đặt" -> ActionRed
        else -> Color.LightGray // Trống
    }
    
    val textColor = if (table.status == "Đã đặt") Color.White else Color.Black

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(table.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
                Text("${table.capacity} người", fontSize = 12.sp, color = textColor.copy(alpha = 0.7f))
            }
        }
    }
}
