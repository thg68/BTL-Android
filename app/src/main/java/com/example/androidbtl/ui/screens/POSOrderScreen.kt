package com.example.androidbtl.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.ui.components.AsyncFoodImage
import com.example.androidbtl.ui.components.EmptyState
import com.example.androidbtl.ui.components.MenuItemSkeleton
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tất cả") }

    val activeOrder = orders.find { it.tableId == tableId }
    val categories = listOf("Tất cả") + menuItems.map { it.category }.distinct()

    val filteredMenuItems = menuItems.filter {
        (selectedCategory == "Tất cả" || it.category == selectedCategory) &&
        (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
    }

    // Advanced UX: Cart Shake Animation logic
    var cartAnimateTrigger by remember { mutableStateOf(0) }
    val cartScale by animateFloatAsState(
        targetValue = if (cartAnimateTrigger > 0) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f),
        label = "cartScale"
    )

    LaunchedEffect(cartAnimateTrigger) {
        if (cartAnimateTrigger > 0) {
            delay(200)
            cartAnimateTrigger = 0
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bàn $tableId", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Saka Hotpot Premium", fontSize = 11.sp, color = BrandYellow, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search toggle */ }) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            val cartItems = activeOrder?.items?.filter { it.status == "Cart" } ?: emptyList()
            val cartCount = cartItems.sumOf { it.quantity }
            val totalAmount = cartItems.sumOf { it.price * it.quantity }

            AnimatedVisibility(
                visible = cartCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF1E1E1E),
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.scale(cartScale)) {
                            Box {
                                Icon(Icons.Filled.LocalMall, contentDescription = null, tint = BrandYellow, modifier = Modifier.size(30.dp))
                                if (cartCount > 0) {
                                    Surface(
                                        color = Color.Red,
                                        shape = CircleShape,
                                        modifier = Modifier.size(18.dp).align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("$cartCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Tổng cộng", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("${"%,.0f".format(totalAmount)}đ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            }
                        }
                        Button(
                            onClick = {
                                if (activeOrder != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.sendOrderToKitchen(activeOrder.id)
                                    onShowMessage("🔥 Đơn hàng đã được gửi xuống bếp!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandYellow),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(50.dp).width(120.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text("GỬI BẾP", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Advanced UX: Modern Category Bar with Icons
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedCategory = category 
                        },
                        label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = {
                            val icon = when(category) {
                                "Thịt bò" -> Icons.Default.Restaurant
                                "Hải sản" -> Icons.Default.WaterDrop
                                "Rau nấm" -> Icons.Default.Eco
                                "Nước lẩu" -> Icons.Default.SoupKitchen
                                "Tráng miệng" -> Icons.Default.Cake
                                else -> Icons.Default.Apps
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandYellow,
                            selectedLabelColor = Color.Black,
                            selectedLeadingIconColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            if (isLoadingMenu) {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(6) { MenuItemSkeleton() }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        // Advanced UX: Promo Card
                        PromoOrderCard()
                    }
                    
                    items(filteredMenuItems) { item ->
                        AdvancedMenuItemCard(item) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            cartAnimateTrigger++
                            if (activeOrder == null) {
                                viewModel.createOrderForTable(tableId)
                                onShowMessage("Đã mở bàn! Hãy thêm lại món nhé.")
                            } else {
                                viewModel.addMenuItemToOrder(activeOrder.id, item)
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
fun PromoOrderCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFBC02D))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Gợi ý cho bạn", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text("Giảm 10% khi gọi từ 5 đĩa bò Mỹ", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun AdvancedMenuItemCard(item: MenuItem, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                AsyncFoodImage(
                    imageUrl = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp))
                )
                if (item.price > 200000) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                    ) {
                        Text("PREMIUM", color = BrandYellow, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${"%,.0f".format(item.price)}đ", color = BrandYellow, fontWeight = FontWeight.Black, fontSize = 17.sp)
            }
            
            IconButton(
                onClick = onAdd,
                modifier = Modifier.background(BrandYellow, CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
            }
        }
    }
}
