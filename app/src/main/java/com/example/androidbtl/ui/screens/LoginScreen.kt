package com.example.androidbtl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidbtl.ui.theme.BrandYellow
import com.example.androidbtl.ui.theme.TextPrimary
import com.example.androidbtl.viewmodel.PosViewModel

@Composable
fun LoginScreen(
    viewModel: PosViewModel,
    onCustomerLogin: (String) -> Unit,
    onStaffLogin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tables by viewModel.tables.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        // Header Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF2D2D2D))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
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
                    CustomerLoginTab(
                        onLogin = onCustomerLogin,
                        checkTableStatus = { id ->
                            val table = tables.find { it.id == id }
                            table?.status ?: "Trống"
                        }
                    )
                } else {
                    StaffLoginTab(onStaffLogin)
                }
            }
        }
    }
}

@Composable
fun CustomerLoginTab(onLogin: (String) -> Unit, checkTableStatus: (String) -> String) {
    var tableId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column {
        Text("Chọn bàn của bạn", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = tableId,
            onValueChange = {
                tableId = it
                errorMessage = null
            },
            placeholder = { Text("Ví dụ: 1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (tableId.isNotBlank()) {
                    val idInt = tableId.toIntOrNull()
                    if (idInt == null || idInt !in 1..15) {
                        errorMessage = "Vui lòng nhập số bàn hợp lệ (1-15)"
                    } else {
                        val status = checkTableStatus(tableId)
                        if (status == "Đang phục vụ") {
                            errorMessage = "Bàn $tableId hiện đã có người ngồi"
                        } else {
                            onLogin(tableId)
                        }
                    }
                }
            },
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
