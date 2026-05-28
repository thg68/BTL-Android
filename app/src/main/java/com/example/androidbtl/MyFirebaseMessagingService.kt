package com.example.androidbtl

import android.util.Log
import com.example.androidbtl.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Gọi khi nhận được thông báo lúc ứng dụng đang ở Foreground (đang mở).
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "Nhận thông báo từ: ${remoteMessage.from}")

        // Kiểm tra nếu thông báo có chứa phần notification payload
        remoteMessage.notification?.let {
            Log.d("FCM", "Nội dung thông báo: ${it.body}")
            
            // Sử dụng NotificationHelper đã viết trước đó để hiển thị thông báo hệ thống
            val helper = NotificationHelper(applicationContext)
            helper.showNotification(
                title = it.title ?: "Thông báo mới",
                message = it.body ?: ""
            )
        }
    }

    /**
     * Gọi khi Token của thiết bị thay đổi hoặc được tạo mới lần đầu.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Token mới: $token")
        
        // Gửi token này lên server/firestore nếu cần để nhắm mục tiêu thiết bị này
        // Lưu ý: Thường sẽ lưu kèm theo UserId hoặc TableId
    }
}
