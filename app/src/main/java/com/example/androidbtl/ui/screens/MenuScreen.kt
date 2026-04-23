package com.example.androidbtl.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.androidbtl.ui.theme.TextPrimary

data class MenuItem(val name: String, val description: String, val price: String)

val menuData = mapOf(
    "Thịt bò" to listOf(
        MenuItem("Bò Wagyu A5", "Thịt bò Wagyu Nhật Bản thượng hạng, mềm tan trong miệng", "299,000đ"),
        MenuItem("Bò Mỹ Thăn Nội", "Thăn nội bò Mỹ cao cấp, vân mỡ đều đẹp", "189,000đ"),
        MenuItem("Bò Úc Sườn Non", "Sườn non bò Úc tươi ngon, ngọt thịt", "149,000đ"),
        MenuItem("Bò Gầu Bò", "Gầu bò tươi, dai giòn sần sật", "89,000đ"),
        MenuItem("Bò Viên Handmade", "Bò viên tự làm từ thịt bò tươi xay", "79,000đ")
    ),
    "Thịt lợn" to listOf(
        MenuItem("Ba Chỉ Lợn Iberico", "Ba chỉ lợn đen Tây Ban Nha thượng hạng", "169,000đ"),
        MenuItem("Lợn Mán Nướng", "Thịt lợn mán tươi, săn chắc thơm ngon", "139,000đ"),
        MenuItem("Xúc Xích Đức", "Xúc xích nhập khẩu từ Đức, đậm đà vị thịt", "99,000đ"),
        MenuItem("Chả Cua", "Chả cua đặc biệt, béo ngậy thơm ngon", "69,000đ"),
        MenuItem("Giò Heo", "Giò heo tươi, da giòn thịt ngọt", "89,000đ")
    ),
    "Hải sản" to listOf(
        MenuItem("Tôm Hùm Alaska", "Tôm hùm Alaska nhập khẩu, thịt săn chắc ngọt", "599,000đ"),
        MenuItem("Cua Hoàng Đế", "Cua hoàng đế Canada, gạch đầy đặn", "459,000đ"),
        MenuItem("Bạch Tuộc Nhật", "Bạch tuộc nhập khẩu từ Nhật Bản, giòn tươi", "199,000đ"),
        MenuItem("Mực Ống Tươi", "Mực ống tươi ngon, thịt trắng trong", "149,000đ"),
        MenuItem("Sò Điệp Nhật", "Sò điệp Nhật Bản size lớn, ngọt thịt", "179,000đ"),
        MenuItem("Tôm Sú", "Tôm sú tươi sống, thịt chắc ngọt", "129,000đ")
    ),
    "Rau nấm" to listOf(
        MenuItem("Nấm Kim Châm", "Nấm kim châm Hàn Quốc, giòn ngon", "49,000đ"),
        MenuItem("Nấm Hương Tươi", "Nấm hương tươi thơm nồng", "59,000đ"),
        MenuItem("Cải Thảo", "Cải thảo tươi xanh, ngọt nước", "39,000đ"),
        MenuItem("Rau Cải Cúc", "Rau cải cúc hữu cơ, thơm đặc trưng", "45,000đ"),
        MenuItem("Bắp Cải Tím", "Bắp cải tím nhập khẩu, giòn ngọt", "55,000đ"),
        MenuItem("Khoai Môn", "Khoai môn tươi, bùi béo", "49,000đ")
    ),
    "Ăn kèm" to listOf(
        MenuItem("Đậu Phụ Non", "Đậu phụ non mềm mịn, hấp thụ vị lẩu", "39,000đ"),
        MenuItem("Mì Udon", "Mì udon Nhật Bản dai giòn", "49,000đ"),
        MenuItem("Bún Tươi", "Bún tươi làm thủ công hàng ngày", "29,000đ"),
        MenuItem("Mì Ramen", "Mì ramen Nhật, sợi mì dai ngon", "59,000đ"),
        MenuItem("Đậu Hũ Ky", "Đậu hũ ky chiên giòn, thấm vị", "49,000đ"),
        MenuItem("Ngô Non", "Ngô non Mỹ, ngọt mềm", "59,000đ")
    ),
    "Tráng miệng" to listOf(
        MenuItem("Pudding Trứng", "Pudding trứng Nhật Bản, mềm mịn tan chảy", "49,000đ"),
        MenuItem("Chè Khúc Bạch", "Chè khúc bạch trái cây tươi", "59,000đ"),
        MenuItem("Kem Mochi", "Kem mochi Nhật Bản nhiều vị", "39,000đ"),
        MenuItem("Sữa Chua Nếp Cẩm", "Sữa chua nếp cẩm truyền thống", "49,000đ"),
        MenuItem("Trái Cây Thập Cẩm", "Đĩa trái cây theo mùa tươi ngon", "69,000đ"),
        MenuItem("Bánh Flan Caramel", "Bánh flan caramel béo mịn", "39,000đ")
    ),
    "Nước lẩu" to listOf(
        MenuItem("Lẩu Tứ Xuyên", "Nước lẩu cay đặc trưng Tứ Xuyên, nồng nàn hương vị", "99,000đ"),
        MenuItem("Lẩu Nấm Thanh Đạm", "Nước lẩu nấm tự nhiên, ngọt thanh", "89,000đ"),
        MenuItem("Lẩu Sa Tế Hải Sản", "Nước lẩu sa tế hải sản đậm đà", "99,000đ"),
        MenuItem("Lẩu Gà Ớt Hiểm", "Nước lẩu gà ớt hiểm cay nồng xuýt xoa", "89,000đ"),
        MenuItem("Lẩu Kim Chi Hàn Quốc", "Nước lẩu kim chi chua cay chuẩn vị Hàn", "99,000đ"),
        MenuItem("Lẩu Xương Khoai Môn", "Nước lẩu xương hầm khoai môn béo ngậy", "89,000đ")
    )
)

@Composable
fun MenuScreen() {
    val categories = menuData.keys.toList()
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    val currentItems = menuData[selectedCategory] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                "Thực Đơn",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = TextPrimary
            )
        }

        // Category Tabs
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) BrandYellow else Color.White)
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        category,
                        color = if (isSelected) Color.Black else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Detailed List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(currentItems) { item ->
                MenuItemCard(
                    title = item.name,
                    description = item.description,
                    price = item.price
                )
            }
        }
    }
}

@Composable
fun MenuItemCard(title: String, description: String, price: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.hotpot_banner),
                contentDescription = "Menu Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(price, color = BrandYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .background(BrandYellow, RoundedCornerShape(24.dp))
                    .size(40.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.Black)
            }
        }
    }
}
