package com.example.androidbtl.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.androidbtl.data.models.MenuItem
import com.example.androidbtl.data.models.Order
import com.example.androidbtl.data.models.OrderItem
import com.example.androidbtl.data.models.RestaurantTable
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PosViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _tables = MutableStateFlow<List<RestaurantTable>>(emptyList())
    val tables: StateFlow<List<RestaurantTable>> = _tables.asStateFlow()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _activeOrders = MutableStateFlow<List<Order>>(emptyList())
    val activeOrders: StateFlow<List<Order>> = _activeOrders.asStateFlow()

    private val _closedOrders = MutableStateFlow<List<Order>>(emptyList())
    val closedOrders: StateFlow<List<Order>> = _closedOrders.asStateFlow()

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
        "Bò Wagyu A5" to "https://images.unsplash.com/photo-1551183053-bf91a1d81141?w=600",
        "Bò Mỹ Thăn Nội" to "https://images.unsplash.com/photo-1588168333986-5078d3ae3976?w=600",
        "Bò Úc Sườn Non" to "https://images.unsplash.com/photo-1588347818133-c8e9b1d3a2c0?w=600",
        "Bò Gầu Bò" to "https://images.unsplash.com/photo-1588168333986-5078d3ae3976?w=600",
        "Bò Viên Handmade" to "https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=600",
        "Ba Chỉ Lợn Iberico" to "https://images.unsplash.com/photo-1607116667981-ff148a4e3e30?w=600",
        "Lợn Mán Nướng" to "https://images.unsplash.com/photo-1544025162-d76694265947?w=600",
        "Xúc Xích Đức" to "https://images.unsplash.com/photo-1601001815853-3835274403b3?w=600",
        "Chả Cua" to "https://images.unsplash.com/photo-1601315379734-425d04812e0e?w=600",
        "Giò Heo" to "https://images.unsplash.com/photo-1544025162-d76694265947?w=600",
        "Tôm Hùm Alaska" to "https://images.unsplash.com/photo-1625943553852-781c6dd46faa?w=600",
        "Cua Hoàng Đế" to "https://images.unsplash.com/photo-1559737558-2f5a35f4523b?w=600",
        "Bạch Tuộc Nhật" to "https://images.unsplash.com/photo-1559737558-2f5a35f4523b?w=600",
        "Mực Ống Tươi" to "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=600",
        "Sò Điệp Nhật" to "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=600",
        "Tôm Sú" to "https://images.unsplash.com/photo-1625943553852-781c6dd46faa?w=600",
        "Nấm Kim Châm" to "https://images.unsplash.com/photo-1611329857570-f02f340e7378?w=600",
        "Nấm Hương Tươi" to "https://images.unsplash.com/photo-1611329857570-f02f340e7378?w=600",
        "Cải Thảo" to "https://images.unsplash.com/photo-1576181256399-834e3b3a49bf?w=600",
        "Rau Cải Cúc" to "https://images.unsplash.com/photo-1576181256399-834e3b3a49bf?w=600",
        "Bắp Cải Tím" to "https://images.unsplash.com/photo-1567375698348-5d9d5ae99de0?w=600",
        "Khoai Môn" to "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=600",
        "Đậu Phụ Non" to "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=600",
        "Mì Udon" to "https://images.unsplash.com/photo-1618841557871-b4664fbf0cb3?w=600",
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
        "Lẩu Tứ Xuyên" to "https://images.unsplash.com/photo-1552611052-33e04de081de?w=600",
        "Lẩu Nấm" to "https://images.unsplash.com/photo-1583032015879-e5022cb87c3b?w=600",
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
                    if (currentUrl.isBlank()) {
                        val name = doc.getString("name").orEmpty()
                        val category = doc.getString("category").orEmpty()
                        val newUrl = imageUrlFor(name, category)
                        doc.reference.update("imageUrl", newUrl)
                        Log.d("PosViewModel", "Backfilled imageUrl for '$name'")
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

    private fun listenToActiveOrders() {
        db.collection("orders").whereEqualTo("status", "Open").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) }
                _activeOrders.value = orders
            }
        }
    }

    private fun listenToClosedOrders() {
        db.collection("orders").whereEqualTo("status", "Closed").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) }
                    .sortedByDescending { it.timestamp }
                _closedOrders.value = orders
            }
        }
    }

    fun updateTableStatus(tableId: String, status: String) {
        db.collection("tables").document(tableId).update("status", status)
    }

    fun createOrderForTable(tableId: String) {
        val newOrder = Order(tableId = tableId, status = "Open")
        db.collection("orders").add(newOrder).addOnSuccessListener {
            updateTableStatus(tableId, "Đang phục vụ")
        }
    }

    fun ensureOrderForTable(tableId: String) {
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
        if (!menuItem.isAvailable) return

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
            val newTotal = order.totalAmount + menuItem.price
            transaction.update(orderRef, mapOf("items" to updatedItems, "totalAmount" to newTotal))
        }
    }

    fun updateOrderItemStatus(orderId: String, itemIndex: Int, newStatus: String) {
        db.collection("orders").document(orderId).get().addOnSuccessListener { doc ->
            val order = doc.toObject(Order::class.java)
            if (order != null) {
                val mutableItems = order.items.toMutableList()
                if (itemIndex in mutableItems.indices) {
                    mutableItems[itemIndex] = mutableItems[itemIndex].copy(status = newStatus)
                    db.collection("orders").document(orderId).update("items", mutableItems)
                }
            }
        }
    }

    fun closeOrder(orderId: String, tableId: String) {
        db.collection("orders").document(orderId).update("status", "Closed").addOnSuccessListener {
            updateTableStatus(tableId, "Trống")
        }
    }

    fun sendOrderToKitchen(orderId: String) {
        db.collection("orders").document(orderId).get().addOnSuccessListener { doc ->
            val order = doc.toObject(Order::class.java) ?: return@addOnSuccessListener
            val cartItems = order.items.filter { it.status == "Cart" }
            if (cartItems.isEmpty()) return@addOnSuccessListener
            
            val updatedItems = order.items.map { 
                if (it.status == "Cart") it.copy(status = "Pending") else it 
            }
            
            db.collection("orders").document(orderId).update(
                mapOf("items" to updatedItems)
            )
        }
    }

    fun removeOrderItem(orderId: String, menuItemId: String) {
        val currentOrders = _activeOrders.value.toMutableList()
        val orderIdx = currentOrders.indexOfFirst { it.id == orderId }
        if (orderIdx != -1) {
            val order = currentOrders[orderIdx]
            val mutableItems = order.items.toMutableList()
            val itemIdx = mutableItems.indexOfFirst { it.menuItemId == menuItemId && it.status == "Cart" }
            if (itemIdx != -1) {
                val removed = mutableItems.removeAt(itemIdx)
                val newTotal = order.totalAmount - (removed.price * removed.quantity)
                currentOrders[orderIdx] = order.copy(items = mutableItems, totalAmount = newTotal)
                _activeOrders.value = currentOrders
            }
        }

        db.collection("orders").document(orderId).get().addOnSuccessListener { doc ->
            val order = doc.toObject(Order::class.java) ?: return@addOnSuccessListener
            val mutableItems = order.items.toMutableList()
            val itemIdx = mutableItems.indexOfFirst { it.menuItemId == menuItemId && it.status == "Cart" }
            if (itemIdx != -1) {
                val removed = mutableItems.removeAt(itemIdx)
                val newTotal = order.totalAmount - (removed.price * removed.quantity)
                db.collection("orders").document(orderId).update(mapOf("items" to mutableItems, "totalAmount" to newTotal))
            }
        }
    }
}
