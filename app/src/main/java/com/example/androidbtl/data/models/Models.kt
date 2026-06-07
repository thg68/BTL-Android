package com.example.androidbtl.data.models

import com.google.firebase.firestore.PropertyName

/**
 * Document bàn trong collection "tables".
 *
 * status điều khiển luồng vận hành:
 * - Trống: khách chưa dùng bàn, có thể đăng nhập bằng nhập tay.
 * - Đang phục vụ: bàn đang có phiên khách, nhập tay bị chặn nhưng QR có accessCode vẫn vào được.
 * - Đã đặt: bàn được giữ chỗ, khách chưa được tự đăng nhập.
 *
 * fcmToken/accessCode là dữ liệu theo phiên bàn. Khi closeTable(), hai field này được xóa
 * để không gửi notification nhầm cho khách cũ và QR cũ không còn hiệu lực.
 */
data class RestaurantTable(
    val id: String = "",
    val name: String = "",
    val status: String = "Trống", // Trống, Đang phục vụ, Đã đặt
    val capacity: Int = 4,
    val fcmToken: String = "", // Lưu token để gửi thông báo đẩy
    val accessCode: String = "" // Mã QR mở bàn hiện tại
)

/**
 * Món trong collection "menu_items".
 *
 * isAvailable map với field Firestore tên "available". Dùng @PropertyName vì Kotlin property
 * là isAvailable nhưng Firestore lưu field ngắn hơn để dễ đọc dữ liệu.
 */
data class MenuItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    @get:PropertyName("available")
    @set:PropertyName("available")
    var isAvailable: Boolean = true,
    val description: String = "",
    val imageUrl: String = ""
)

/**
 * Một dòng món trong Order.items.
 *
 * status là trạng thái đi qua pipeline:
 * - Cart: khách đã chọn nhưng chưa gửi bếp.
 * - Pending: đã gửi bếp, đang chờ làm.
 * - Cooking: bếp đang chế biến.
 * - Done: món đã xong, app có thể gửi thông báo về bàn.
 */
data class OrderItem(
    val menuItemId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val status: String = "Pending" // Pending, Cooking, Done, Cart
)

/**
 * Hóa đơn/order của một bàn.
 *
 * status:
 * - Open: bàn đang dùng hóa đơn này để thêm món và tính tiền.
 * - Closed: nhân viên đã xác nhận thanh toán; order được dùng cho báo cáo doanh thu.
 */
data class Order(
    val id: String = "",
    val tableId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "Open", // Open, Closed
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Thông báo nội bộ cho nhân viên.
 *
 * targetRoute giúp AppNavigation mở đúng tab khi bấm thông báo, ví dụ:
 * - billing: xác nhận thanh toán.
 * - tables: sơ đồ bàn/gọi nhân viên.
 * - kds: bếp xử lý món mới.
 */
data class NotificationItem(
    val id: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetRoute: String = ""
)
