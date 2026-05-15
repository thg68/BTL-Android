package com.example.androidbtl.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.ui.components.AsyncFoodImage
import com.example.androidbtl.ui.components.EmptyState
import com.example.androidbtl.ui.components.MenuItemSkeleton
import com.example.androidbtl.ui.theme.BrandYellow
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
    val isLoadingMenu by viewModel.isLoadingMenu.collectAsState()
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Order - Bàn $tableId",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
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
                    val cartCount = cartItems.sumOf { it.quantity }
                    val cartScale by animateFloatAsState(
                        targetValue = if (cartCount > 0) 1f else 0.95f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
                        label = "cartScale"
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (onNavigateToBooking != null && activeOrder != null) {
                            OutlinedButton(
                                onClick = onNavigateToBooking,
                                modifier = Modifier
                                    .height(50.dp)
                                    .scale(cartScale),
                                border = BorderStroke(1.5.dp, BrandYellow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "GIỎ HÀNG ($cartCount)",
                                    color = BrandYellow,
                                    fontWeight = FontWeight.ExtraBold
                                )
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
                .background(MaterialTheme.colorScheme.background)
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
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (isLoadingMenu) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(6) { MenuItemSkeleton() }
                }
            } else if (filteredMenuItems.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.RestaurantMenu,
                    title = if (searchQuery.isBlank()) "Chưa có món ăn nào" else "Không tìm thấy món",
                    description = if (searchQuery.isBlank()) "" else "Thử tìm kiếm với từ khác"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
}

@Composable
fun PosMenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncFoodImage(
                imageUrl = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.description.isNotBlank()) {
                    Text(
                        item.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    "${"%,.0f".format(item.price)}đ",
                    color = BrandYellow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
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
