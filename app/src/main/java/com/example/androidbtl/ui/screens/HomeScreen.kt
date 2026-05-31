package com.example.androidbtl.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.ui.components.AsyncFoodImage
import com.example.androidbtl.ui.components.DishCardSkeleton
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun HomeScreen(
    tableId: String,
    viewModel: PosViewModel,
    onNavigateToMenu: () -> Unit,
    onNavigateToBill: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onShowMessage: (String) -> Unit = {}
) {
    val topSellingItems by viewModel.topSellingItems.collectAsStateWithLifecycle()
    val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    
    var selectedDish by remember { mutableStateOf<MenuItem?>(null) }

    val currentOrder = activeOrders.find { it.tableId == tableId }
    val pendingCount = currentOrder?.items?.count { it.status == "Pending" } ?: 0
    val cookingCount = currentOrder?.items?.count { it.status == "Cooking" } ?: 0
    val doneCount = currentOrder?.items?.count { it.status == "Done" } ?: 0

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Chào buổi sáng"
        in 12..17 -> "Chào buổi chiều"
        else -> "Chào buổi tối"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(greeting, fontSize = 13.sp, color = BrandYellow, fontWeight = FontWeight.ExtraBold)
                            Text(
                                "Bàn $tableId",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Surface(
                            onClick = onNavigateToProfile,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape,
                            modifier = Modifier.size(52.dp),
                            shadowElevation = 3.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                    AnimatedVisibility(visible = (pendingCount + cookingCount + doneCount) > 0, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        OrderProgressGlassCard(pendingCount, cookingCount, doneCount, onNavigateToBill)
                    }
                    if ((pendingCount + cookingCount + doneCount) > 0) Spacer(modifier = Modifier.height(24.dp))
                    BannerSection()
                    Spacer(modifier = Modifier.height(32.dp))
                    QuickActionsGrid(onMenu = onNavigateToMenu, onBill = onNavigateToBill, onCallStaff = { viewModel.callStaff(tableId); onShowMessage("Đã thông báo cho nhân viên. Chúng tôi sẽ đến ngay!") })
                    Spacer(modifier = Modifier.height(32.dp))
                    FeaturedDishesSection(title = "THỰC ĐƠN XU HƯỚNG", featured = topSellingItems, isLoading = menuItems.isEmpty(), onViewAllClick = onNavigateToMenu, onDishClick = { selectedDish = it })
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
        DishDetailOverlay(dish = selectedDish, onDismiss = { selectedDish = null }, onOrderClick = { onNavigateToMenu(); selectedDish = null })
    }
}

@Composable
fun DishDetailOverlay(dish: MenuItem?, onDismiss: () -> Unit, onOrderClick: () -> Unit) {
    AnimatedVisibility(visible = dish != null, enter = fadeIn() + scaleIn(initialScale = 0.8f), exit = fadeOut() + scaleOut(targetScale = 0.8f)) {
        if (dish != null) {
            Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
                    Card(modifier = Modifier.fillMaxWidth(0.85f).wrapContentHeight().clickable(enabled = false) {}, shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column {
                            AsyncFoodImage(imageUrl = dish.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)))
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(dish.name, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                    Surface(color = BrandYellow.copy(alpha = 0.2f), shape = CircleShape) { Text("HOT", color = BrandYellow, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(dish.description.ifBlank { "Món ăn đặc sắc được chế biến từ nguyên liệu tươi ngon nhất trong ngày của nhà hàng." }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${"%,.0f".format(dish.price)}đ", fontSize = 24.sp, fontWeight = FontWeight.Black, color = BrandYellow)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(onClick = onOrderClick, colors = ButtonDefaults.buttonColors(containerColor = Color.Black), shape = RoundedCornerShape(16.dp)) { Text("ĐẶT NGAY", color = Color.White, fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderProgressGlassCard(pending: Int, cooking: Int, done: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E).copy(alpha = 0.95f)), elevation = CardDefaults.cardElevation(defaultElevation = 12.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BrandYellow, shape = CircleShape, modifier = Modifier.size(8.dp)) {}
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("TIẾN ĐỘ PHỤC VỤ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = BrandYellow, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ProgressIconItem("Đã gửi", pending, Icons.AutoMirrored.Filled.Send, pending > 0, Color(0xFF90CAF9))
                ProgressIconItem("Đang nấu", cooking, Icons.Default.Whatshot, cooking > 0, Color(0xFFFFB74D))
                ProgressIconItem("Sẵn sàng", done, Icons.Default.CheckCircle, done > 0, Color(0xFF81C784))
            }
        }
    }
}

@Composable
fun ProgressIconItem(label: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean, activeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Surface(modifier = Modifier.size(52.dp), color = if (isActive) activeColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f), shape = CircleShape, border = if (isActive) BorderStroke(1.5.dp, activeColor) else null) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = if (isActive) activeColor else Color.Gray, modifier = Modifier.size(24.dp)) }
            }
            if (count > 0) {
                Surface(color = Color.Red, shape = CircleShape, modifier = Modifier.size(20.dp).align(Alignment.TopEnd)) {
                    Box(contentAlignment = Alignment.Center) { Text("$count", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black) }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(label, fontSize = 11.sp, color = if (isActive) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun QuickActionsGrid(onMenu: () -> Unit, onBill: () -> Unit, onCallStaff: () -> Unit) {
    var isPulsing by remember { mutableStateOf(false) }
    val pulseScale by animateFloatAsState(targetValue = if (isPulsing) 0.9f else 1f, animationSpec = spring(dampingRatio = 0.3f, stiffness = 500f))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActionBigCard("Thực đơn", Icons.AutoMirrored.Filled.MenuBook, BrandYellow, Modifier.weight(1f), onMenu)
        ActionBigCard("Thanh toán", Icons.Default.Payments, Color(0xFF81C784), Modifier.weight(1f), onBill)
        Surface(onClick = { isPulsing = true; onCallStaff() }, modifier = Modifier.weight(1f).height(100.dp).scale(pulseScale), shape = RoundedCornerShape(24.dp), color = Color(0xFFE57373).copy(alpha = 0.1f), border = BorderStroke(1.dp, Color(0xFFE57373).copy(alpha = 0.3f))) {
            LaunchedEffect(isPulsing) { if (isPulsing) { delay(150); isPulsing = false } }
            Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gọi phục vụ", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFFE57373))
            }
        }
    }
}

@Composable
fun ActionBigCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier.height(100.dp), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun FeaturedDishesSection(title: String, featured: List<MenuItem>, isLoading: Boolean, onViewAllClick: () -> Unit, onDishClick: (MenuItem) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black)
            TextButton(onClick = onViewAllClick) { Text("Xem tất cả", color = BrandYellow, fontWeight = FontWeight.Bold) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) { LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(5) { DishCardSkeleton() } } }
        else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                items(featured, key = { it.id }) { item ->
                    Card(modifier = Modifier.width(200.dp).clip(RoundedCornerShape(28.dp)).clickable { onDishClick(item) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column {
                            Box {
                                AsyncFoodImage(imageUrl = item.imageUrl, contentDescription = null, modifier = Modifier.height(130.dp).fillMaxWidth())
                                Surface(color = Color(0xFFFF5252), shape = RoundedCornerShape(bottomEnd = 16.dp), modifier = Modifier.align(Alignment.TopStart)) { Text("TRENDING", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) }
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${"%,.0f".format(item.price)}đ", color = BrandYellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = BrandYellow, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BannerSection() {
    Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth().height(180.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Sử dụng AsyncFoodImage để nạp banner an toàn hơn thay vì painterResource trực tiếp
            AsyncFoodImage(imageUrl = "", contentDescription = null, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(brush = Brush.horizontalGradient(colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent))))
            Column(modifier = Modifier.align(Alignment.CenterStart).padding(24.dp)) {
                Surface(color = BrandYellow, shape = RoundedCornerShape(10.dp)) { Text("HOT DEAL", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black) }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Đại Tiệc Buffet\nThượng Hạng", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, lineHeight = 28.sp)
            }
        }
    }
}
