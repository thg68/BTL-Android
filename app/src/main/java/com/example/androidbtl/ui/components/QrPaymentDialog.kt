package com.example.androidbtl.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.androidbtl.data.VietQrConfig
import com.example.androidbtl.data.VietQrGenerator
import com.example.androidbtl.ui.theme.BrandYellow
import qrcode.QRCode

/**
 * Dialog QR thanh toán VietQR cho một bàn.
 * Payload được tạo từ số tiền hiện tại và nội dung chuyển khoản BAN<tableId>.
 */
@Composable
fun QrPaymentDialog(
    amount: Double,
    tableId: String,
    onDismiss: () -> Unit
) {
    val amountLong = amount.toLong()
    // Nội dung chuyển khoản gắn theo bàn để nhân viên dễ đối soát giao dịch trong app ngân hàng.
    val description = "BAN$tableId"

    val qrBitmap: ImageBitmap = remember(amountLong, description) {
        // Tạo QR một lần cho mỗi cặp số tiền/nội dung để tránh render lại khi recomposition.
        val payload = VietQrGenerator.build(
            bankBin = VietQrConfig.MBBANK_BIN,
            accountNo = VietQrConfig.BANK_ACCOUNT_NUMBER,
            accountName = VietQrConfig.BANK_ACCOUNT_NAME,
            amount = amountLong,
            description = description
        )
        val bytes = QRCode.ofSquares()
            .withSize(12)
            .build(payload)
            .renderToBytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.QrCode2,
                        contentDescription = null,
                        tint = BrandYellow,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Quét QR để thanh toán",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(12.dp)
                ) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "QR thanh toán",
                        modifier = Modifier.size(240.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "${"%,d".format(amountLong)}đ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandYellow
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(label = "Ngân hàng", value = VietQrConfig.BANK_DISPLAY_NAME)
                InfoRow(label = "Số tài khoản", value = VietQrConfig.BANK_ACCOUNT_NUMBER)
                InfoRow(label = "Chủ tài khoản", value = VietQrConfig.BANK_ACCOUNT_NAME)
                InfoRow(label = "Nội dung", value = description)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Sau khi chuyển khoản thành công, vui lòng đợi nhân viên xác nhận để hoàn tất.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text("ĐÓNG", color = BrandYellow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
