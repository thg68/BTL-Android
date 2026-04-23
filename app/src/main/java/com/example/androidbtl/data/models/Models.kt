package com.example.androidbtl.data.models

data class RestaurantTable(
    val id: String = "",
    val name: String = "",
    val status: String = "Trống", // Trống, Đang phục vụ, Đã đặt
    val capacity: Int = 4
)

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val isAvailable: Boolean = true
)

data class OrderItem(
    val menuItemId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val status: String = "Pending" // Pending, Cooking, Done
)

data class Order(
    val id: String = "",
    val tableId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "Open", // Open, Closed
    val timestamp: Long = System.currentTimeMillis()
)
