package com.example.androidbtl.utils

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executors

/**
 * Gửi push notification bằng Firebase Cloud Messaging HTTP v1 API.
 *
 * App đang gửi trực tiếp từ thiết bị bằng service_account.json trong assets. Cách này tiện cho bài tập/demo,
 * nhưng trong sản phẩm thật nên chuyển sang backend hoặc Cloud Functions để không nhúng service account
 * vào APK.
 */
object FcmSender {
    private const val FCM_V1_URL = "https://fcm.googleapis.com/v1/projects/androidbtl-d09e0/messages:send"
    private val client = OkHttpClient()

    fun sendNotification(context: Context, targetToken: String, title: String, message: String) {
        if (targetToken.isBlank()) {
            Log.e("FCM_V1", "Không tìm thấy Token khách hàng")
            return
        }

        Executors.newSingleThreadExecutor().execute {
            try {
                // HTTP v1 API cần OAuth access token lấy từ service account.
                val accessToken = getAccessToken(context)
                if (accessToken.isNullOrBlank()) {
                    Log.e("FCM_V1", "Không lấy được Access Token")
                    return@execute
                }

                val notification = JSONObject().apply {
                    put("title", title)
                    put("body", message)
                }

                val androidNotification = JSONObject().apply {
                    // Icon phải là drawable trắng đơn sắc để Android hiển thị đúng trên notification shade.
                    put("icon", "ic_notification_saka")
                    put("color", "#FFC107")
                }

                val androidConfig = JSONObject().apply {
                    put("notification", androidNotification)
                }

                val messageObj = JSONObject().apply {
                    put("token", targetToken)
                    put("notification", notification)
                    put("android", androidConfig)
                }

                val root = JSONObject().apply {
                    put("message", messageObj)
                }

                val body = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(FCM_V1_URL)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("FCM_V1", "Gửi thất bại: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val resBody = response.body?.string()
                        if (response.isSuccessful) {
                            Log.d("FCM_V1", "Đã gửi thông báo tới khách hàng thành công!")
                        } else {
                            Log.e("FCM_V1", "Lỗi từ Google: $resBody")
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("FCM_V1", "Lỗi gửi FCM (Kiểm tra service_account.json): ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    private fun getAccessToken(context: Context): String? {
        return try {
            // service_account.json phải nằm trong app/src/main/assets.
            // GoogleCredentials refresh token khi hết hạn trước khi gửi request FCM.
            val inputStream = context.assets.open("service_account.json")
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
            googleCredentials.refreshIfExpired()
            googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e("FCM_V1", "Lỗi khi đọc service_account.json: ${e.message}")
            null
        }
    }
}
