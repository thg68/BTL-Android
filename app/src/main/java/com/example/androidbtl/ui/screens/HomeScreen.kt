package com.example.androidbtl.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.R
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.ui.components.AsyncFoodImage
import com.example.androidbtl.ui.components.DishCardSkeleton
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.viewmodel.PosViewModel
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
    val topSellingItems by viewModel.topSellingItems.collectAsState()
    val activeOrders by viewModel.activeOrders.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()

    val currentOrder = activeOrders.find { it.tableId == tableId }
    val pendingCount = currentOrder?.items?.count { it.status == "Pending" } ?: 0
    val cookingCount = currentOrder?.items?.count { it.status == "Cooking" } ?: 0
    val doneCount = currentOrder?.items?.count { it.status == "Done" } ?: 0

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Chào buổi sáng"
        in 12..17 -> "Chào buổi chiều"
        else -> "Chào buổi tối"
    }

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
            // Header Section với hiệu ứng Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF2D2D2D), Color.Transparent)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(greeting, fontSize = 14.sp, color = BrandYellow, fontWeight = FontWeight.Medium)
                        Text("Bàn $tableId", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp).clickable { onNavigateToProfile() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Advanced UX: Real-time Order Progress Hub
                AnimatedVisibility(
                    visible = (pendingCount + cookingCount + doneCount) > 0,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    OrderProgressHub(pendingCount, cookingCount, doneCount, onNavigateToBill)
                }

                Spacer(modifier = Modifier.height(16.dp))
                BannerSection()
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Dịch vụ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                QuickActionsRow(onNavigateToMenu, onNavigateToBill, onCallStaff = {
                    viewModel.callStaff(tableId)
                    onShowMessage("Đã phát tín hiệu gọi nhân viên tới bàn $tableId!")
                })

                Spacer(modifier = Modifier.height(28.dp))
                
                // Advanced UX: Trending Section
                FeaturedDishesSection(
                    title = "🔥 Món đang HOT",
                    featured = topSellingItems,
                    isLoading = menuItems.isEmpty(),
                    onViewAllClick = onNavigateToMenu
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun OrderProgressHub(pending: Int, cooking: Int, done: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trạng thái phục vụ", color = Color.White, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BrandYellow)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressStep("Đã gửi", pending, Icons.Default.Send, pending > 0)
                ProgressStep("Đang nấu", cooking, Icons.Default.Fireplace, cooking > 0)
                ProgressStep("Xong", done, Icons.Default.CheckCircle, done > 0)
            }
        }
    }
}

@Composable
fun ProgressStep(label: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(45.dp),
                color = if (isActive) BrandYellow else Color.White.copy(alpha = 0.05f),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = if (isActive) Color.Black else Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
            if (count > 0) {
                Surface(
                    color = Color.Red,
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp).align(Alignment.TopEnd)
                ) {
                    Text("$count", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.wrapContentSize())
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 11.sp, color = if (isActive) Color.White else Color.Gray)
    }
}

@Composable
fun QuickActionsRow(onMenu: () -> Unit, onBill: () -> Unit, onCallStaff: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionCard("Thực đơn", Icons.Default.MenuBook, BrandYellow, Modifier.weight(1f), onMenu)
        QuickActionCard("Thanh toán", Icons.Default.Payments, Color(0xFF81C784), Modifier.weight(1f), onBill)
        QuickActionCard("Gọi NV", Icons.Default.NotificationsActive, Color(0xFFE57373), Modifier.weight(1f), onCallStaff)
    }
}

@Composable
fun QuickActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeaturedDishesSection(title: String, featured: List<MenuItem>, isLoading: Boolean, onViewAllClick: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            TextButton(onClick = onViewAllClick) { Text("Tất cả", color = BrandYellow) }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (isLoading) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(5) { DishCardSkeleton() } }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(featured) { item ->
                    Card(
                        modifier = Modifier.width(180.dp).clip(RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            AsyncFoodImage(imageUrl = item.imageUrl, contentDescription = null, modifier = Modifier.height(120.dp).fillMaxWidth())
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${"%,.0f".format(item.price)}đ", color = BrandYellow, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = BrandYellow)
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
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(160.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(id = R.drawable.hotpot_banner), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Surface(color = BrandYellow, shape = RoundedCornerShape(8.dp)) {
                    Text("HOT DEAL", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Text("Buffet Đặc Biệt", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
        }
    }
}
