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
            Column {
                TopAppBar(
                    title = { Text("Order - Bàn $tableId", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cartItems = activeOrder?.items?.filter { it.status == "Cart" } ?: emptyList()

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (onNavigateToBooking != null && activeOrder != null) {
                            OutlinedButton(
                                onClick = onNavigateToBooking,
                                modifier = Modifier.height(50.dp),
                                border = BorderStroke(1.5.dp, BrandYellow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("GIỎ HÀNG (${cartItems.sumOf { it.quantity }})", color = BrandYellow, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Button(
                            onClick = {
                                if (activeOrder == null) {
                                    viewModel.createOrderForTable(tableId)
                                    onShowMessage("Đã mở bàn $tableId")
                                } else {
                                    val cart = activeOrder.items.filter { it.status == "Cart" }
                                    if (cart.isNotEmpty()) {
                                        viewModel.sendOrderToKitchen(activeOrder.id)
                                        onShowMessage("Đã gửi ${cart.sumOf { it.quantity }} món xuống bếp!")
                                    } else {
                                        onShowMessage("Giỏ hàng trống!")
                                    }
                                }
                            },
                            modifier = Modifier.height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandYellow),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (activeOrder == null) "MỞ BÀN" else "GỬI BẾP", 
                                color = Color.Black, 
                                fontWeight = FontWeight.ExtraBold
                            )
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
                    .padding(16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                label = { Text("Tìm kiếm món ăn...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandYellow,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (filteredMenuItems.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Chưa có món ăn nào...", color = Color.Gray)
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
                item { Spacer(modifier = Modifier.height(100.dp)) }
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
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                Text("${"%,.0f".format(item.price)}đ", color = BrandYellow, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
            Surface(
                color = BrandYellow,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.Black)
                }
            }
        }
    }
}
