package com.example.androidbtl.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.R
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary

@Composable
fun LoginScreen(
    onCustomerLogin: (String) -> Unit,
    onStaffLogin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        // Header Area with Fallback Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF2D2D2D))
        ) {
            Text(
                "SAKA SYSTEM",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White.copy(alpha = 0.1f),
                fontSize = 60.sp,
                fontWeight = FontWeight.Black
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        // Title
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SAKA SYSTEM", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Hệ thống quản lý nhà hàng", color = BrandYellow)
        }

        // Login Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = BrandYellow
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("KHÁCH HÀNG", color = if (selectedTab == 0) BrandYellow else Color.Gray, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("NHÂN VIÊN", color = if (selectedTab == 1) BrandYellow else Color.Gray, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedTab == 0) {
                    CustomerLoginTab(onCustomerLogin)
                } else {
                    StaffLoginTab(onStaffLogin)
                }
            }
        }
    }
}

@Composable
fun CustomerLoginTab(onLogin: (String) -> Unit) {
    var tableId by remember { mutableStateOf("") }
    
    Column {
        Text("Chọn bàn của bạn", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = tableId,
            onValueChange = { tableId = it },
            placeholder = { Text("Ví dụ: 1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { if (tableId.isNotBlank()) onLogin(tableId) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandYellow)
        ) {
            Text("BẮT ĐẦU GỌI MÓN", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StaffLoginTab(onLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; showError = false },
            label = { Text("Tên đăng nhập") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (showError) {
            Text("Sai tài khoản hoặc mật khẩu (Dùng: admin/123)", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (username == "admin" && password == "123") {
                    onLogin()
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("ĐĂNG NHẬP HỆ THỐNG", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
