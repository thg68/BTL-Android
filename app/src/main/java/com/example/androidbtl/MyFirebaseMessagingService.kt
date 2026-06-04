package com.example.androidbtl

import android.util.Log
import com.example.androidbtl.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Nhận FCM khi app đang mở và chuyển sang notification hệ thống.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "Nhận thông báo từ: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d("FCM", "Nội dung thông báo: ${it.body}")
            
            val helper = NotificationHelper(applicationContext)
            helper.showNotification(
                title = it.title ?: "Thông báo mới",
                message = it.body ?: ""
            )
        }
    }

    /**
     * Token đổi thì ghi log để dễ đăng ký lại thiết bị nhận FCM khi cần.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Token mới: $token")
    }
}
