package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun TableManagementScreen(viewModel: PosViewModel, onTableClick: (String, String) -> Unit) {
    val tables by viewModel.tables.collectAsState()
    val isLoading by viewModel.isLoadingTables.collectAsState()

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
                    "Sơ đồ Bàn",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                items(tables) { table ->
                    TableCard(table = table) {
                        onTableClick(table.id, table.status)
                    }
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
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

@Composable
fun TableCard(table: RestaurantTable, onClick: () -> Unit) {
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
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    table.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = textColor
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
            }
        }
    }
}
