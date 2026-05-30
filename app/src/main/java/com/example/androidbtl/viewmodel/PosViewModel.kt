package com.example.androidbtl.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.data.models.NotificationItem
import com.example.androidbtl.data.models.Order
import com.example.androidbtl.data.models.OrderItem
import com.example.androidbtl.data.models.RestaurantTable
import com.example.androidbtl.utils.FcmSender
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()

    private val _tables = MutableStateFlow<List<RestaurantTable>>(emptyList())
    val tables: StateFlow<List<RestaurantTable>> = _tables.asStateFlow()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _activeOrders = MutableStateFlow<List<Order>>(emptyList())
    val activeOrders: StateFlow<List<Order>> = _activeOrders.asStateFlow()

    private val _closedOrders = MutableStateFlow<List<Order>>(emptyList())
    val closedOrders: StateFlow<List<Order>> = _closedOrders.asStateFlow()

    private val _newOrderEvent = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val newOrderEvent: SharedFlow<String> = _newOrderEvent.asSharedFlow()

    private val _dishReadyEvent = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 10)
    val dishReadyEvent: SharedFlow<Pair<String, String>> = _dishReadyEvent.asSharedFlow()

    val pendingItemCount: StateFlow<Int> = _activeOrders.map { orders ->
        orders.sumOf { order -> order.items.count { it.status == "Pending" } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val topSellingItems: StateFlow<List<MenuItem>> = combine(_menuItems, _closedOrders) { menu, closed ->
        if (closed.isEmpty()) {
            menu.filter { it.isAvailable }.take(6)
        } else {
            val counts = closed.flatMap { it.items }.groupingBy { it.menuItemId }.eachCount()
            menu.filter { it.isAvailable }
                .sortedByDescending { counts[it.id] ?: 0 }
                .take(6)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    val unreadCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun markAllRead() {
        _notifications.update { current -> current.map { it.copy(isRead = true) } }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }

    private val _isLoadingMenu = MutableStateFlow(true)
    val isLoadingMenu: StateFlow<Boolean> = _isLoadingMenu.asStateFlow()

    private val _isLoadingTables = MutableStateFlow(true)
    val isLoadingTables: StateFlow<Boolean> = _isLoadingTables.asStateFlow()

    init {
        listenToTables()
        listenToMenuItems()
        listenToActiveOrders()
        listenToClosedOrders()
        seedDatabaseIfEmpty()
        backfillImageUrls()
    }

    private val nameToImageUrl: Map<String, String> = mapOf(
        "Bò Wagyu A5" to "https://images.unsplash.com/photo-1615937691194-97dbd3f3dc29?w=600",
        "Bò Mỹ Thăn Nội" to "https://images.unsplash.com/photo-1618400109127-b9c068eabe1b?w=600",
        "Bò Úc Sườn Non" to "https://plus.unsplash.com/premium_photo-1668616817170-2a74b5cd181d?w=600",
        "Bò Gầu Bò" to "https://images.unsplash.com/photo-1719785046032-20b6470e19e0?w=600",
        "Bò Viên Handmade" to "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=600",
        "Ba Chỉ Lợn Iberico" to "https://images.unsplash.com/photo-1752555535777-0aed7bc93f98?w=600",
        "Lợn Mán Nướng" to "https://images.unsplash.com/photo-1624174782964-e541742299ee?w=600",
        "Xúc Xích Đức" to "https://images.unsplash.com/photo-1601001815853-3835274403b3?w=600",
        "Chả Cua" to "https://images.unsplash.com/photo-1601315379734-425d04812e0e?w=600",
        "Giò Heo" to "https://images.unsplash.com/photo-1544025162-d76694265947?w=600",
        "Tôm Hùm Alaska" to "https://images.unsplash.com/photo-1707995546402-5057206e5161?w=600",
        "Cua Hoàng Đế" to "https://images.unsplash.com/photo-1573655554431-f3c4d0c3869d?w=600",
        "Bạch Tuộc Nhật" to "https://images.unsplash.com/photo-1559737558-2f5a35f4523b?w=600",
        "Mực Ống Tươi" to "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=600",
        "Sò Điệp Nhật" to "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=600",
        "Tôm Sú" to "https://images.unsplash.com/photo-1625943553852-781c6dd46faa?w=600",
        "Nấm Kim Châm" to "https://images.unsplash.com/photo-1769195045391-a970e273e07f?w=600",
        "Nấm Hương Tươi" to "https://images.unsplash.com/photo-1611329857570-f02f340e7378?w=600",
        "Cải Thảo" to "https://images.unsplash.com/photo-1576181256399-834e3b3a49bf?w=600",
        "Rau Cải Cúc" to "https://images.unsplash.com/photo-1576181256399-834e3b3a49bf?w=600",
        "Bắp Cải Tím" to "https://images.unsplash.com/photo-1567375698348-5d9d5ae99de0?w=600",
        "Khoai Môn" to "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=600",
        "Đậu Phụ Non" to "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=600",
        "Mì Udon" to "https://images.unsplash.com/photo-1599314250681-8e05113e0e1b?w=600",
        "Bún Tươi" to "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600",
        "Mì Ramen" to "https://images.unsplash.com/photo-1618841557871-b4664fbf0cb3?w=600",
        "Đậu Hũ Ky" to "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=600",
        "Ngô Non" to "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=600",
        "Pudding Trứng" to "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600",
        "Chè Khúc Bạch" to "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600",
        "Kem Mochi" to "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600",
        "Sữa Chua Nếp Cẩm" to "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600",
        "Trái Cây Thập Cẩm" to "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?w=600",
        "Bánh Flan Caramel" to "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600",
        "Lẩu Tứ Xuyên" to "https://images.unsplash.com/photo-1611345157614-26d3bdd10c93?w=600",
        "Lẩu Nấm" to "https://images.unsplash.com/photo-1767354063743-a296165b0685?w=600",
        "Lẩu Nấm Thanh Đạm" to "https://images.unsplash.com/photo-1583032015879-e5022cb87c3b?w=600",
        "Lẩu Sa Tế Hải Sản" to "https://images.unsplash.com/photo-1552611052-33e04de081de?w=600",
        "Lẩu Gà Ớt Hiểm" to "https://images.unsplash.com/photo-1552611052-33e04de081de?w=600",
        "Lẩu Kim Chi Hàn Quốc" to "https://images.unsplash.com/photo-1552611052-33e04de081de?w=600",
        "Lẩu Xương Khoai Môn" to "https://images.unsplash.com/photo-1583032015879-e5022cb87c3b?w=600"
    )

    private val categoryFallbackImage: Map<String, String> = mapOf(
        "Thịt bò" to "https://images.unsplash.com/photo-1588168333986-5078d3ae3976?w=600",
        "Thịt lợn" to "https://images.unsplash.com/photo-1544025162-d76694265947?w=600",
        "Hải sản" to "https://images.unsplash.com/photo-1559737558-2f5a35f4523b?w=600",
        "Rau nấm" to "https://images.unsplash.com/photo-1611329857570-f02f340e7378?w=600",
        "Ăn kèm" to "https://images.unsplash.com/photo-1618841557871-b4664fbf0cb3?w=600",
        "Tráng miệng" to "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=600",
        "Nước lẩu" to "https://images.unsplash.com/photo-1552611052-33e04de081de?w=600"
    )

    private val defaultImage = "https://images.unsplash.com/photo-1552611052-33e04de081de?w=600"

    private fun imageUrlFor(name: String, category: String): String =
        nameToImageUrl[name] ?: categoryFallbackImage[category] ?: defaultImage

    private fun seedDatabaseIfEmpty() {
        db.collection("menu_items").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.size() < 5) {
                    Log.d("PosViewModel", "Seeding more menu items...")
                    val sampleItems = listOf(
                        MenuItem(name = "Bò Wagyu A5", category = "Thịt bò", price = 299000.0, description = "Thịt bò Wagyu Nhật Bản thượng hạng"),
                        MenuItem(name = "Bò Mỹ Thăn Nội", category = "Thịt bò", price = 189000.0, description = "Thăn nội bò Mỹ cao cấp"),
                        MenuItem(name = "Ba Chỉ Lợn Iberico", category = "Thịt lợn", price = 169000.0, description = "Ba chỉ lợn đen Tây Ban Nha"),
                        MenuItem(name = "Lợn Mán Nướng", category = "Thịt lợn", price = 139000.0, description = "Thịt lợn mán tươi ngon"),
                        MenuItem(name = "Tôm Hùm Alaska", category = "Hải sản", price = 599000.0, description = "Tôm hùm tươi sống nhập khẩu"),
                        MenuItem(name = "Cua Hoàng Đế", category = "Hải sản", price = 459000.0, description = "Cua hoàng đế Canada"),
                        MenuItem(name = "Nấm Kim Châm", category = "Rau nấm", price = 49000.0, description = "Nấm tươi sạch mỗi ngày"),
                        MenuItem(name = "Lẩu Tứ Xuyên", category = "Nước lẩu", price = 99000.0, description = "Nước lẩu cay nồng đặc trưng"),
                        MenuItem(name = "Lẩu Nấm", category = "Nước lẩu", price = 89000.0, description = "Nước lẩu nấm thanh đạm"),
                        MenuItem(name = "Mì Udon", category = "Ăn kèm", price = 49000.0, description = "Mì Nhật Bản dai ngon")
                    ).map { it.copy(imageUrl = imageUrlFor(it.name, it.category)) }

                    sampleItems.forEach { item ->
                        if (snapshot.documents.none { it.getString("name") == item.name }) {
                            db.collection("menu_items").add(item)
                        }
                    }
                }
            }

        db.collection("tables").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.size() < 15) {
                    Log.d("PosViewModel", "Seeding 15 tables...")
                    for (i in 1..15) {
                        val tableId = i.toString()
                        if (snapshot.documents.none { it.id == tableId }) {
                            val table = RestaurantTable(id = tableId, name = "Bàn $tableId", status = "Trống", capacity = 4)
                            db.collection("tables").document(tableId).set(table)
                        }
                    }
                }
            }
    }

    private fun backfillImageUrls() {
        db.collection("menu_items").get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val currentUrl = doc.getString("imageUrl").orEmpty()
                    val name = doc.getString("name").orEmpty()
                    val category = doc.getString("category").orEmpty()
                    val expectedUrl = imageUrlFor(name, category)
                    if (currentUrl != expectedUrl) {
                        doc.reference.update("imageUrl", expectedUrl)
                        Log.d("PosViewModel", "Synced imageUrl for '$name'")
                    }
                }
            }
    }

    private fun listenToTables() {
        db.collection("tables").addSnapshotListener { snapshot, e ->
            if (e != null) {
                _isLoadingTables.value = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val tableList = snapshot.documents.mapNotNull { it.toObject(RestaurantTable::class.java)?.copy(id = it.id) }
                    .sortedBy { it.id.toIntOrNull() ?: 0 }
                _tables.value = tableList
                _isLoadingTables.value = false
            }
        }
    }

    private fun listenToMenuItems() {
        db.collection("menu_items").addSnapshotListener { snapshot, e ->
            if (e != null) {
                _isLoadingMenu.value = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { it.toObject(MenuItem::class.java)?.copy(id = it.id) }
                _menuItems.value = items
                _isLoadingMenu.value = false
            }
        }
    }

    private val prevPendingCount = mutableMapOf<String, Int>()
    private val prevDoneItemCounts = mutableMapOf<String, Map<String, Int>>()
    private var ordersInitialized = false

    private fun listenToActiveOrders() {
        db.collection("orders").whereEqualTo("status", "Open").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("PosViewModel", "Listen active orders failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                try {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(id = doc.id)?.let { order ->
                            order.copy(totalAmount = order.items.sumOf { it.price * it.quantity })
                        }
                    }

                    if (ordersInitialized) {
                        orders.forEach { order ->
                            val newCount = order.items.count { it.status == "Pending" }
                            val oldCount = prevPendingCount[order.id] ?: 0
                            if (newCount > oldCount) {
                                val diff = newCount - oldCount
                                val msg = "Bàn ${order.tableId} vừa gửi $diff món mới!"
                                val notif = NotificationItem(
                                    id = "${order.id}_${System.currentTimeMillis()}",
                                    message = msg
                                )
                                viewModelScope.launch {
                                    _notifications.update { current -> listOf(notif) + current }
                                    _newOrderEvent.emit(msg)
                                }
                            }

                            val currentDoneCounts = order.items.filter { it.status == "Done" }
                                .groupingBy { it.name }.eachCount()
                            val previousDoneCounts = prevDoneItemCounts[order.id] ?: emptyMap()

                            currentDoneCounts.forEach { (itemName, count) ->
                                val prevCount = previousDoneCounts[itemName] ?: 0
                                if (count > prevCount) {
                                    val msg = "món $itemName của bạn đã xong, Vui lòng chờ ít phút để được phục vụ"
                                    viewModelScope.launch {
                                        _dishReadyEvent.emit(Pair(order.tableId, msg))
                                    }
                                }
                            }
                            prevDoneItemCounts[order.id] = currentDoneCounts
                        }
                    }

                    prevPendingCount.clear()
                    orders.forEach { order ->
                        prevPendingCount[order.id] = order.items.count { it.status == "Pending" }
                        if (!ordersInitialized) {
                            prevDoneItemCounts[order.id] = order.items.filter { it.status == "Done" }
                                .groupingBy { it.name }.eachCount()
                        }
                    }
                    ordersInitialized = true
                    _activeOrders.value = orders
                } catch (ex: Exception) {
                    Log.e("PosViewModel", "Error processing active orders", ex)
                }
            }
        }
    }

    private fun listenToClosedOrders() {
        db.collection("orders").whereEqualTo("status", "Closed").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val orders = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)?.let { order ->
                        order.copy(totalAmount = order.items.sumOf { it.price * it.quantity })
                    }
                }.sortedByDescending { it.timestamp }
                _closedOrders.value = orders
            }
        }
    }

    fun updateTableStatus(tableId: String, status: String) {
        if (tableId.isBlank()) return
        db.collection("tables").document(tableId).update("status", status)
    }

    fun updateTableFcmToken(tableId: String, token: String) {
        if (tableId.isBlank()) return
        db.collection("tables").document(tableId).update("fcmToken", token)
            .addOnSuccessListener { Log.d("FCM", "Đã cập nhật Token cho bàn $tableId") }
    }

    fun addTable(name: String, capacity: Int) {
        val docRef = db.collection("tables").document()
        docRef.set(RestaurantTable(id = docRef.id, name = name, status = "Trống", capacity = capacity))
    }

    fun updateTable(tableId: String, name: String, capacity: Int) {
        if (tableId.isBlank()) return
        db.collection("tables").document(tableId).update(mapOf("name" to name, "capacity" to capacity))
    }

    fun deleteTable(tableId: String) {
        if (tableId.isBlank()) return
        db.collection("tables").document(tableId).delete()
    }

    fun createOrderForTable(tableId: String) {
        if (tableId.isBlank()) return
        val newOrder = Order(tableId = tableId, status = "Open")
        db.collection("orders").add(newOrder).addOnSuccessListener {
            updateTableStatus(tableId, "Đang phục vụ")
        }
    }

    fun ensureOrderForTable(tableId: String) {
        if (tableId.isBlank()) return
        db.collection("orders")
            .whereEqualTo("tableId", tableId)
            .whereEqualTo("status", "Open")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    createOrderForTable(tableId)
                } else {
                    updateTableStatus(tableId, "Đang phục vụ")
                }
            }
    }

    fun updateMenuItemAvailability(menuItemId: String, isAvailable: Boolean) {
        db.collection("menu_items").document(menuItemId).update("available", isAvailable)
    }

    fun addMenuItemToOrder(orderId: String, menuItem: MenuItem) {
        if (orderId.isBlank() || !menuItem.isAvailable) return

        val currentOrders = _activeOrders.value.toMutableList()
        val orderIdx = currentOrders.indexOfFirst { it.id == orderId }
        if (orderIdx != -1) {
            val order = currentOrders[orderIdx]
            val updatedItems = order.items.toMutableList()
            val existingIdx = updatedItems.indexOfFirst { it.menuItemId == menuItem.id && it.status == "Cart" }
            if (existingIdx >= 0) {
                val existingItem = updatedItems[existingIdx]
                updatedItems[existingIdx] = existingItem.copy(quantity = existingItem.quantity + 1)
            } else {
                updatedItems.add(OrderItem(menuItemId = menuItem.id, name = menuItem.name, quantity = 1, price = menuItem.price, status = "Cart"))
            }
            currentOrders[orderIdx] = order.copy(items = updatedItems, totalAmount = order.totalAmount + menuItem.price)
            _activeOrders.value = currentOrders
        }

        val orderRef = db.collection("orders").document(orderId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(orderRef)
            val order = snapshot.toObject(Order::class.java) ?: return@runTransaction
            val existingIndex = order.items.indexOfFirst { it.menuItemId == menuItem.id && it.status == "Cart" }
            val updatedItems = order.items.toMutableList()
            if (existingIndex >= 0) {
                val existingItem = updatedItems[existingIndex]
                updatedItems[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
            } else {
                updatedItems.add(OrderItem(menuItemId = menuItem.id, name = menuItem.name, quantity = 1, price = menuItem.price, status = "Cart"))
            }
            val newTotal = updatedItems.sumOf { it.price * it.quantity }
            transaction.update(orderRef, mapOf("items" to updatedItems, "totalAmount" to newTotal))
        }
    }

    fun updateOrderItemStatus(orderId: String, itemIndex: Int, newStatus: String) {
        if (orderId.isBlank()) return
        db.collection("orders").document(orderId).get().addOnSuccessListener { doc ->
            val order = doc.toObject(Order::class.java)
            if (order != null) {
                val mutableItems = order.items.toMutableList()
                if (itemIndex in mutableItems.indices) {
                    val item = mutableItems[itemIndex]
                    mutableItems[itemIndex] = item.copy(status = newStatus)
                    db.collection("orders").document(orderId).update("items", mutableItems)
                        .addOnSuccessListener {
                            if (newStatus == "Done") {
                                sendFcmToTable(order.tableId, "Món ăn hoàn tất", "Món ${item.name} của bạn đã sẵn sàng!")
                            }
                        }
                }
            }
        }
    }

    private fun sendFcmToTable(tableId: String, title: String, message: String) {
        if (tableId.isBlank()) return
        db.collection("tables").document(tableId).get().addOnSuccessListener { doc ->
            val token = doc.getString("fcmToken") ?: ""
            if (token.isNotBlank()) {
                FcmSender.sendNotification(getApplication(), token, title, message)
            } else {
                Log.e("FCM", "Không tìm thấy Token cho bàn $tableId")
            }
        }
    }

    fun closeOrder(orderId: String, tableId: String) {
        if (orderId.isBlank()) return
        try {
            val order = _activeOrders.value.find { it.id == orderId }
            val total = order?.items?.sumOf { it.price * it.quantity } ?: 0.0
            
            db.collection("orders").document(orderId)
                .update(mapOf(
                    "status" to "Closed", 
                    "totalAmount" to total,
                    "timestamp" to System.currentTimeMillis()
                ))
                .addOnSuccessListener { 
                    if (tableId.isNotBlank()) updateTableStatus(tableId, "Trống")
                    Log.d("PosViewModel", "Order $orderId closed successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("PosViewModel", "Failed to close order $orderId", e)
                }
        } catch (ex: Exception) {
            Log.e("PosViewModel", "Crash in closeOrder", ex)
        }
    }

    fun sendOrderToKitchen(orderId: String) {
        if (orderId.isBlank()) return
        val order = _activeOrders.value.find { it.id == orderId } ?: return
        val cartItems = order.items.filter { it.status == "Cart" }
        if (cartItems.isEmpty()) return

        val updatedItems = order.items.map {
            if (it.status == "Cart") it.copy(status = "Pending") else it
        }

        _activeOrders.update { current ->
            current.map { if (it.id == orderId) it.copy(items = updatedItems) else it }
        }

        db.collection("orders").document(orderId).update("items", updatedItems)
    }

    fun addMenuItem(name: String, category: String, price: Double, description: String, imageUrl: String) {
        val url = imageUrl.ifBlank { imageUrlFor(name, category) }
        val item = MenuItem(name = name, category = category, price = price, description = description, imageUrl = url)
        db.collection("menu_items").add(item)
    }

    fun updateMenuItem(item: MenuItem) {
        if (item.id.isBlank()) return
        db.collection("menu_items").document(item.id).set(item)
    }

    fun findMenuItemById(id: String): MenuItem? {
        return _menuItems.value.find { it.id == id }
    }

    fun deleteMenuItem(menuItemId: String) {
        if (menuItemId.isBlank()) return
        db.collection("menu_items").document(menuItemId).delete()
    }

    fun removeOrderItem(orderId: String, menuItemId: String) {
        if (orderId.isBlank()) return
        
        _activeOrders.update { current ->
            current.map { order ->
                if (order.id == orderId) {
                    val mutableItems = order.items.toMutableList()
                    val itemIdx = mutableItems.indexOfFirst { it.menuItemId == menuItemId && it.status == "Cart" }
                    if (itemIdx != -1) {
                        val removed = mutableItems.removeAt(itemIdx)
                        val newTotal = order.totalAmount - (removed.price * removed.quantity)
                        order.copy(items = mutableItems, totalAmount = newTotal)
                    } else order
                } else order
            }
        }

        val updatedOrder = _activeOrders.value.find { it.id == orderId } ?: return
        db.collection("orders").document(orderId)
            .update(mapOf("items" to updatedOrder.items, "totalAmount" to updatedOrder.totalAmount))
    }

    fun callStaff(tableId: String) {
        if (tableId.isBlank()) return
        val msg = "Bàn $tableId đang gọi nhân viên!"
        val notif = NotificationItem(
            id = "call_${tableId}_${System.currentTimeMillis()}",
            message = msg
        )
        viewModelScope.launch {
            _notifications.update { current -> listOf(notif) + current }
            _newOrderEvent.emit(msg)
        }
    }
}
