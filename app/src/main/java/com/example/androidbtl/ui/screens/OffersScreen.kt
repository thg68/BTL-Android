package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary

data class Offer(
    val title: String,
    val description: String,
    val code: String,
    val expiry: String
)

/**
 * Tab ưu đãi của khách hàng. Dữ liệu hiện là danh sách tĩnh trong UI.
 */
@Composable
fun OffersScreen() {
    val offers = remember {
        listOf(
            Offer("Giảm 20% Bò Wagyu", "Áp dụng cho khách hàng gọi món tại bàn.", "WAGYU20", "Hạn dùng: 31/12/2026"),
            Offer("Đi 4 Tính Tiền 3", "Dành cho nhóm khách đi lẩu buổi trưa.", "GROUP4", "Hạn dùng: 30/06/2026"),
            Offer("Tặng Pudding Trứng", "Mỗi hóa đơn trên 500k tặng ngay 1 phần tráng miệng.", "FREEPUD", "Hạn dùng: 31/12/2026"),
            Offer("Giảm 10% Tổng Bill", "Ưu đãi dành cho thành viên mới đăng ký.", "SakaNEW", "Hạn dùng: Không giới hạn")
        )
    }

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
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Ưu Đãi Hiện Tại",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(offers, key = { it.code }) { offer ->
                OfferCard(offer)
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun OfferCard(offer: Offer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = BrandYellow.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.LocalOffer,
                        contentDescription = null,
                        tint = BrandYellow,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    offer.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    offer.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            offer.code,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = BrandYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        offer.expiry,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
