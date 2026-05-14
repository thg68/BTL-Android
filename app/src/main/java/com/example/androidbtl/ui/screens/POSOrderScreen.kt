package com.example.androidbtl.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary
import com.example.androidbtl.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSOrderScreen(
    tableId: String,
    viewModel: PosViewModel,
    onNavigateToBooking: (() -> Unit)? = null,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val orders by viewModel.activeOrders.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Tìm đơn hàng đang hoạt động của bàn này
    val activeOrder = orders.find { it.tableId == tableId }
    val filteredMenuItems = if (searchQuery.isBlank()) {
        menuItems
    } else {
        menuItems.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order - Bàn $tableId") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cartItems = activeOrder?.items?.filter { it.status == "Cart" } ?: emptyList()

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onNavigateToBooking != null && activeOrder != null) {
                            OutlinedButton(
                                onClick = onNavigateToBooking,
                                modifier = Modifier.height(48.dp),
                                border = BorderStroke(1.dp, BrandYellow)
                            ) {
                                Text("GIỎ HÀNG (${cartItems.sumOf { it.quantity }})", color = BrandYellow, fontWeight = FontWeight.Bold)
                            }
                        }
                        Button(
                            onClick = {
                                if (activeOrder == null) {
                                    viewModel.createOrderForTable(tableId)
                                    onShowMessage("Đã mở bàn $tableId")
                                } else {
                                    val cartItems = activeOrder.items.filter { it.status == "Cart" }
                                    if (cartItems.isNotEmpty()) {
                                        viewModel.sendOrderToKitchen(activeOrder.id)
                                        onShowMessage("Đã gửi ${cartItems.sumOf { it.quantity }} món xuống bếp!")
                                    } else {
                                        onShowMessage("Giỏ hàng trống!")
                                    }
                                }
                            },
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandYellow)
                        ) {
                            Text(if (activeOrder == null) "MỞ BÀN" else "GỬI BẾP", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                label = { Text("Tìm món") },
                placeholder = { Text("Nhập tên món hoặc danh mục") }
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredMenuItems.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Đang tải thực đơn hoặc không tìm thấy món...", color = Color.Gray)
                        }
                    }
                }
                items(filteredMenuItems) { item ->
                    PosMenuItemCard(item) {
                        if (activeOrder != null) {
                            viewModel.addMenuItemToOrder(activeOrder.id, item)
                        } else {
                            onShowMessage("Vui lòng nhấn 'MỞ BÀN' trước!")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PosMenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("${"%,.0f".format(item.price)}đ", color = BrandYellow, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandYellow)
                    .padding(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.Black)
            }
        }
    }
}
