package com.example.androidbtl.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.R
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.BrandYellowLight
import com.example.androidbtl.ui.theme.TextPrimary

@Composable
fun HomeScreen(onNavigateToMenu: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        GreetingSection()
        Spacer(modifier = Modifier.height(24.dp))
        BannerSection()
        Spacer(modifier = Modifier.height(24.dp))
        QuickActions(onNavigateToMenu)
        Spacer(modifier = Modifier.height(24.dp))
        FeaturedDishesSection()
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun GreetingSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Xin chào, Khách hàng", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Chào mừng bạn đến với lẩu SaKa!", fontSize = 14.sp, color = Color.Gray)
        }
        Icon(Icons.Filled.Star, contentDescription = null, tint = BrandYellow, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun BannerSection() {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.hotpot_banner),
                contentDescription = "Promo Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    "Đại Tiệc Bò Wagyu",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    "Chỉ từ 199k/người",
                    color = BrandYellow,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun QuickActions(onNavigateToMenu: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionItem("Đặt Bàn Mới", Icons.Filled.Star, Color(0xFFFFE082)) { /* TODO */ }
        QuickActionItem("Giao Hàng", Icons.Filled.Star, Color(0xFFE1BEE7)) { /* TODO */ }
        QuickActionItem("Thực Đơn", Icons.Filled.RestaurantMenu, Color(0xFFC8E6C9)) { onNavigateToMenu() }
    }
}

@Composable
fun QuickActionItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(60.dp)
                .background(bgColor, RoundedCornerShape(16.dp))
        ) {
            Icon(icon, contentDescription = title, tint = TextPrimary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
fun FeaturedDishesSection() {
    Column {
        Text("Món Phải Thử", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(10) {
                DishCard()
            }
        }
    }
}

@Composable
fun DishCard() {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.hotpot_banner),
                contentDescription = "Dish Image",
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Bò Mỹ Cao Cấp", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("69,000đ", color = BrandYellow, fontWeight = FontWeight.Bold)
            }
        }
    }
}
