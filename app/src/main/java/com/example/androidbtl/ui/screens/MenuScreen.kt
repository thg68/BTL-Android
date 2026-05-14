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
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary

val menuData = mapOf(
    "Thịt bò" to listOf(
        MenuItem(name = "Bò Wagyu A5", description = "Thịt bò Wagyu Nhật Bản thượng hạng, mềm tan trong miệng", price = 299000.0, category = "Thịt bò"),
        MenuItem(name = "Bò Mỹ Thăn Nội", description = "Thăn nội bò Mỹ cao cấp, vân mỡ đều đẹp", price = 189000.0, category = "Thịt bò"),
        MenuItem(name = "Bò Úc Sườn Non", description = "Sườn non bò Úc tươi ngon, ngọt thịt", price = 149000.0, category = "Thịt bò"),
        MenuItem(name = "Bò Gầu Bò", description = "Gầu bò tươi, dai giòn sần sật", price = 89000.0, category = "Thịt bò"),
        MenuItem(name = "Bò Viên Handmade", description = "Bò viên tự làm từ thịt bò tươi xay", price = 79000.0, category = "Thịt bò")
    ),
    "Thịt lợn" to listOf(
        MenuItem(name = "Ba Chỉ Lợn Iberico", description = "Ba chỉ lợn đen Tây Ban Nha thượng hạng", price = 169000.0, category = "Thịt lợn"),
        MenuItem(name = "Lợn Mán Nướng", description = "Thịt lợn mán tươi, săn chắc thơm ngon", price = 139000.0, category = "Thịt lợn"),
        MenuItem(name = "Xúc Xích Đức", description = "Xúc xích nhập khẩu từ Đức, đậm đà vị thịt", price = 99000.0, category = "Thịt lợn"),
        MenuItem(name = "Chả Cua", description = "Chả cua đặc biệt, béo ngậy thơm ngon", price = 69000.0, category = "Thịt lợn"),
        MenuItem(name = "Giò Heo", description = "Giò heo tươi, da giòn thịt ngọt", price = 89000.0, category = "Thịt lợn")
    ),
    "Hải sản" to listOf(
        MenuItem(name = "Tôm Hùm Alaska", description = "Tôm hùm Alaska nhập khẩu, thịt săn chắc ngọt", price = 599000.0, category = "Hải sản"),
        MenuItem(name = "Cua Hoàng Đế", description = "Cua hoàng đế Canada, gạch đầy đặn", price = 459000.0, category = "Hải sản"),
        MenuItem(name = "Bạch Tuộc Nhật", description = "Bạch tuộc nhập khẩu từ Nhật Bản, giòn tươi", price = 199000.0, category = "Hải sản"),
        MenuItem(name = "Mực Ống Tươi", description = "Mực ống tươi ngon, thịt trắng trong", price = 149000.0, category = "Hải sản"),
        MenuItem(name = "Sò Điệp Nhật", description = "Sò điệp Nhật Bản size lớn, ngọt thịt", price = 179000.0, category = "Hải sản"),
        MenuItem(name = "Tôm Sú", description = "Tôm sú tươi sống, thịt chắc ngọt", price = 129000.0, category = "Hải sản")
    ),
    "Rau nấm" to listOf(
        MenuItem(name = "Nấm Kim Châm", description = "Nấm kim châm Hàn Quốc, giòn ngon", price = 49000.0, category = "Rau nấm"),
        MenuItem(name = "Nấm Hương Tươi", description = "Nấm hương tươi thơm nồng", price = 59000.0, category = "Rau nấm"),
        MenuItem(name = "Cải Thảo", description = "Cải thảo tươi xanh, ngọt nước", price = 39000.0, category = "Rau nấm"),
        MenuItem(name = "Rau Cải Cúc", description = "Rau cải cúc hữu cơ, thơm đặc trưng", price = 45000.0, category = "Rau nấm"),
        MenuItem(name = "Bắp Cải Tím", description = "Bắp cải tím nhập khẩu, giòn ngọt", price = 55000.0, category = "Rau nấm"),
        MenuItem(name = "Khoai Môn", description = "Khoai môn tươi, bùi béo", price = 49000.0, category = "Rau nấm")
    ),
    "Ăn kèm" to listOf(
        MenuItem(name = "Đậu Phụ Non", description = "Đậu phụ non mềm mịn, hấp thụ vị lẩu", price = 39000.0, category = "Ăn kèm"),
        MenuItem(name = "Mì Udon", description = "Mì udon Nhật Bản dai giòn", price = 49000.0, category = "Ăn kèm"),
        MenuItem(name = "Bún Tươi", description = "Bún tươi làm thủ công hàng ngày", price = 29000.0, category = "Ăn kèm"),
        MenuItem(name = "Mì Ramen", description = "Mì ramen Nhật, sợi mì dai ngon", price = 59000.0, category = "Ăn kèm"),
        MenuItem(name = "Đậu Hũ Ky", description = "Đậu hũ ky chiên giòn, thấm vị", price = 49000.0, category = "Ăn kèm"),
        MenuItem(name = "Ngô Non", description = "Ngô non Mỹ, ngọt mềm", price = 59000.0, category = "Ăn kèm")
    ),
    "Tráng miệng" to listOf(
        MenuItem(name = "Pudding Trứng", description = "Pudding trứng Nhật Bản, mềm mịn tan chảy", price = 49000.0, category = "Tráng miệng"),
        MenuItem(name = "Chè Khúc Bạch", description = "Chè khúc bạch trái cây tươi", price = 59000.0, category = "Tráng miệng"),
        MenuItem(name = "Kem Mochi", description = "Kem mochi Nhật Bản nhiều vị", price = 39000.0, category = "Tráng miệng"),
        MenuItem(name = "Sữa Chua Nếp Cẩm", description = "Sữa chua nếp cẩm truyền thống", price = 49000.0, category = "Tráng miệng"),
        MenuItem(name = "Trái Cây Thập Cẩm", description = "Đĩa trái cây theo mùa tươi ngon", price = 69000.0, category = "Tráng miệng"),
        MenuItem(name = "Bánh Flan Caramel", description = "Bánh flan caramel béo mịn", price = 39000.0, category = "Tráng miệng")
    ),
    "Nước lẩu" to listOf(
        MenuItem(name = "Lẩu Tứ Xuyên", description = "Nước lẩu cay đặc trưng Tứ Xuyên, nồng nàn hương vị", price = 99000.0, category = "Nước lẩu"),
        MenuItem(name = "Lẩu Nấm Thanh Đạm", description = "Nước lẩu nấm tự nhiên, ngọt thanh", price = 89000.0, category = "Nước lẩu"),
        MenuItem(name = "Lẩu Sa Tế Hải Sản", description = "Nước lẩu sa tế hải sản đậm đà", price = 99000.0, category = "Nước lẩu"),
        MenuItem(name = "Lẩu Gà Ớt Hiểm", description = "Nước lẩu gà ớt hiểm cay nồng xuýt xoa", price = 89000.0, category = "Nước lẩu"),
        MenuItem(name = "Lẩu Kim Chi Hàn Quốc", description = "Nước lẩu kim chi chua cay chuẩn vị Hàn", price = 99000.0, category = "Nước lẩu"),
        MenuItem(name = "Lẩu Xương Khoai Môn", description = "Nước lẩu xương hầm khoai môn béo ngậy", price = 89000.0, category = "Nước lẩu")
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
fun MenuItemCard(title: String, description: String, price: Double) {
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
                Text("${"%,.0f".format(price)}đ", color = BrandYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
