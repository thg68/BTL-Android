package com.example.androidbtl.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.androidbtl.data.models.RestaurantTable
import com.example.androidbtl.viewmodel.PosViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private data class LoginColors(
    val background: Color,
    val card: Color,
    val panel: Color,
    val muted: Color,
    val stroke: Color,
    val text: Color,
    val primary: Color,
    val onPrimary: Color,
    val tabTrack: Color,
    val fieldContainer: Color
)

private data class TableQrPayload(
    val tableId: String,
    val accessCode: String?
)

@Composable
private fun rememberLoginColors(): LoginColors {
    val scheme = MaterialTheme.colorScheme
    return LoginColors(
        background = scheme.background,
        card = scheme.surface,
        panel = scheme.surface,
        muted = scheme.onSurfaceVariant,
        stroke = scheme.outline.copy(alpha = 0.32f),
        text = scheme.onSurface,
        primary = scheme.primary,
        onPrimary = scheme.onPrimary,
        tabTrack = scheme.surfaceVariant.copy(alpha = 0.72f),
        fieldContainer = scheme.surface
    )
}

/**
 * Màn đăng nhập chung.
 *
 * Khách có hai đường vào:
 * - Nhập số bàn thủ công: chỉ được vào bàn chưa phục vụ.
 * - Quét QR bàn: có accessCode của phiên bàn nên có thể vào lại bàn đang phục vụ.
 *
 * Nhân viên đăng nhập bằng luồng staff riêng để vào cụm tab vận hành.
 */
@Composable
fun LoginScreen(
    viewModel: PosViewModel,
    onCustomerLogin: (String, String?, Boolean) -> Unit,
    onStaffLogin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val loginColors = rememberLoginColors()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(loginColors.background)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = loginColors.card),
            border = BorderStroke(1.dp, loginColors.stroke),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginBrandHeader()

                HorizontalDivider(
                    modifier = Modifier.padding(top = 28.dp, bottom = 26.dp),
                    color = loginColors.primary.copy(alpha = 0.16f)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = loginColors.panel,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, loginColors.stroke)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        LoginSegmentedTabs(
                            selectedTab = selectedTab,
                            onSelectedTabChange = { selectedTab = it }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (selectedTab == 0) {
                            CustomerLoginTab(
                                onLogin = onCustomerLogin,
                                tables = tables
                            )
                        } else {
                            StaffLoginTab(onStaffLogin)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Gọi món nhanh", color = loginColors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("  •  ", color = loginColors.primary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Phục vụ chính xác", color = loginColors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun LoginBrandHeader() {
    val loginColors = rememberLoginColors()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(78.dp),
            shape = CircleShape,
            color = loginColors.primary.copy(alpha = 0.18f),
            border = BorderStroke(1.2.dp, loginColors.primary.copy(alpha = 0.36f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = loginColors.card,
                    border = BorderStroke(1.dp, loginColors.primary.copy(alpha = 0.22f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = loginColors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "SAKA",
                color = loginColors.text,
                fontSize = 44.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp
            )
            Text(".", color = loginColors.primary, fontSize = 46.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text(
            "HOTPOT EXPERIENCE",
            color = loginColors.muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )
    }
}

@Composable
private fun LoginSegmentedTabs(
    selectedTab: Int,
    onSelectedTabChange: (Int) -> Unit
) {
    val loginColors = rememberLoginColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = loginColors.tabTrack,
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LoginTabPill(
                title = "Khách hàng",
                selected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { onSelectedTabChange(0) }
            )
            LoginTabPill(
                title = "Nhân viên",
                selected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { onSelectedTabChange(1) }
            )
        }
    }
}

@Composable
private fun LoginTabPill(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val loginColors = rememberLoginColors()
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(15.dp),
        color = if (selected) loginColors.primary else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                title,
                color = if (selected) loginColors.onPrimary else loginColors.muted,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun CustomerLoginTab(onLogin: (String, String?, Boolean) -> Unit, tables: List<RestaurantTable>) {
    val context = LocalContext.current
    val loginColors = rememberLoginColors()
    var tableId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showScanner by remember { mutableStateOf(false) }

    fun submitTable(id: String, accessCode: String? = null, fromQr: Boolean = false) {
        val normalizedId = id.trim()
        val normalizedAccessCode = accessCode.orEmpty()
        // Rule bảo vệ phiên bàn:
        // - fromQr=false: khách tự nhập số bàn, không được vào bàn Đang phục vụ.
        // - fromQr=true + accessCode: khách đang quét QR của phiên bàn hiện tại.
        //
        // Nhờ rule này, nhóm khách cùng bàn có thể quét lại QR để dùng chung phiên,
        // nhưng người ngoài chỉ nhập số bàn thì không chiếm được bàn đang có khách.
        val hasQrAccess = fromQr && normalizedAccessCode.isNotBlank()
        val table = tables.find { it.id == normalizedId }
        when {
            normalizedId.isBlank() -> errorMessage = "Vui lòng nhập số bàn"
            tables.isEmpty() -> errorMessage = "Đang tải danh sách bàn, vui lòng thử lại"
            table == null -> errorMessage = "Không tìm thấy bàn $normalizedId"
            table.status == "Đã đặt" -> errorMessage = "Bàn $normalizedId đã được đặt trước"
            table.status == "Đang phục vụ" && !hasQrAccess -> {
                errorMessage = "Bàn $normalizedId đang có khách sử dụng"
            }
            else -> onLogin(normalizedId, accessCode, hasQrAccess)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) showScanner = true
        else errorMessage = "Vui lòng cấp quyền camera để quét QR"
    }

    Column {
        Text(
            "SỐ BÀN",
            color = loginColors.muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = tableId,
            onValueChange = {
                tableId = it
                errorMessage = null
            },
            leadingIcon = {
                Icon(Icons.Filled.TableRestaurant, contentDescription = null, tint = loginColors.muted, modifier = Modifier.size(20.dp))
            },
            placeholder = { Text("Nhập số bàn...", color = loginColors.muted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = loginColors.text,
                unfocusedTextColor = loginColors.text,
                cursorColor = loginColors.primary,
                focusedBorderColor = loginColors.primary,
                unfocusedBorderColor = loginColors.stroke,
                focusedContainerColor = loginColors.fieldContainer,
                unfocusedContainerColor = loginColors.fieldContainer
            ),
            shape = RoundedCornerShape(16.dp)
        )

        val currentTable = tables.find { it.id == tableId.trim() }
        if (currentTable?.status == "Đang phục vụ") {
            Text(
                text = "Bàn ${tableId.trim()} đang có khách sử dụng.",
                color = loginColors.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
        if (currentTable?.status == "Đã đặt") {
            Text(
                text = "Bàn ${tableId.trim()} đã được đặt trước.",
                color = loginColors.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 7.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = { submitTable(tableId) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = loginColors.primary)
        ) {
            Text(
                if (currentTable?.status == "Trống" || currentTable == null) "BẮT ĐẦU GỌI MÓN" else "BÀN KHÔNG KHẢ DỤNG",
                color = loginColors.onPrimary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = loginColors.stroke)
            Text("  HOẶC  ", color = loginColors.muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = loginColors.stroke)
        }

        OutlinedButton(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    showScanner = true
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, loginColors.stroke),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = loginColors.primary)
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = loginColors.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Quét mã QR bàn", color = loginColors.primary, fontWeight = FontWeight.ExtraBold)
        }
    }

    if (showScanner) {
        QrTableScannerDialog(
            onDismiss = { showScanner = false },
            onQrScanned = { rawValue ->
                showScanner = false
                val scannedPayload = parseTableQrPayload(rawValue)
                if (scannedPayload == null) {
                    errorMessage = "QR không hợp lệ"
                } else {
                    tableId = scannedPayload.tableId
                    submitTable(scannedPayload.tableId, scannedPayload.accessCode, fromQr = true)
                }
            }
        )
    }
}

@Composable
fun StaffLoginTab(onLogin: () -> Unit) {
    val loginColors = rememberLoginColors()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column {
        Text("TÀI KHOẢN", color = loginColors.muted, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it; showError = false },
            placeholder = { Text("Tên đăng nhập", color = loginColors.muted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = loginTextFieldColors(),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("MẬT KHẨU", color = loginColors.muted, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            placeholder = { Text("Mật khẩu", color = loginColors.muted) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = loginTextFieldColors(),
            shape = RoundedCornerShape(16.dp)
        )
        if (showError) {
            Text(
                "Sai tài khoản hoặc mật khẩu",
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = {
                if (username == "admin" && password == "123") onLogin()
                else showError = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = loginColors.primary)
        ) {
            Text("ĐĂNG NHẬP HỆ THỐNG", color = loginColors.onPrimary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun loginTextFieldColors() = rememberLoginColors().let { loginColors ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = loginColors.text,
        unfocusedTextColor = loginColors.text,
        cursorColor = loginColors.primary,
        focusedBorderColor = loginColors.primary,
        unfocusedBorderColor = loginColors.stroke,
        focusedContainerColor = loginColors.fieldContainer,
        unfocusedContainerColor = loginColors.fieldContainer
    )
}

@Composable
private fun QrTableScannerDialog(
    onDismiss: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Quét QR bàn",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Đưa mã QR vào giữa khung quét.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CameraQrScanner(
                        modifier = Modifier.fillMaxSize(),
                        onQrScanned = onQrScanned
                    )
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Hủy", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraQrScanner(
    modifier: Modifier = Modifier,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    val didScan = remember { AtomicBoolean(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage == null) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            val rawValue = barcodes.firstOrNull()?.rawValue
                                            if (!rawValue.isNullOrBlank() && didScan.compareAndSet(false, true)) {
                                                ContextCompat.getMainExecutor(context).execute {
                                                    onQrScanned(rawValue)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                }
                            }

                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    },
                    ContextCompat.getMainExecutor(ctx)
                )
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
            scanner.close()
            cameraExecutor.shutdown()
        }
    }
}

/**
 * Parse nội dung QR bàn.
 *
 * App hỗ trợ hai dạng:
 * - Deep link androidbtl://table/{id}?code=... do TableManagementScreen tạo.
 * - Chuỗi đơn giản chỉ chứa số bàn để tương thích với QR cũ hoặc test nhanh.
 *
 * Nếu có code, LoginScreen truyền tiếp accessCode sang AppNavigation để xử lý như login từ QR.
 */
private fun parseTableQrPayload(rawValue: String): TableQrPayload? {
    val value = rawValue.trim()
    if (value.isBlank()) return null

    return runCatching {
        val uri = Uri.parse(value)
        if (uri.scheme == "androidbtl" && uri.host == "table") {
            uri.lastPathSegment
                ?.takeIf { it.isNotBlank() }
                ?.let { tableId -> TableQrPayload(tableId = tableId, accessCode = uri.getQueryParameter("code")) }
        } else {
            value
                .takeIf { it.matches(Regex("[A-Za-z0-9_-]+")) }
                ?.let { tableId -> TableQrPayload(tableId = tableId, accessCode = null) }
        }
    }.getOrNull()
}
