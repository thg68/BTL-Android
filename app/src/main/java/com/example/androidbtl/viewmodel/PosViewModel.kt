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

    init {
        listenToTables()
        listenToMenuItems()
        listenToActiveOrders()
        seedDatabaseIfEmpty()
    }

    private fun seedDatabaseIfEmpty() {
        db.collection("menu_items").get()
            .addOnSuccessListener { snapshot ->
                // Nếu thực đơn quá ít, tiến hành nạp thêm dữ liệu mẫu phong phú
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
                    )
                    sampleItems.forEach { item ->
                        // Chỉ thêm nếu món đó chưa tồn tại trong DB (kiểm tra theo tên)
                        if (snapshot.documents.none { it.getString("name") == item.name }) {
                            db.collection("menu_items").add(item)
                        }
                    }
                }
            }
        
        db.collection("tables").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Log.d("PosViewModel", "Database tables empty, seeding...")
                    for (i in 1..6) {
                        val table = RestaurantTable(id = i.toString(), name = "Bàn $i")
                        db.collection("tables").document(i.toString()).set(table)
                    }
                }
            }
    }

    private fun listenToTables() {
        db.collection("tables").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val tableList = snapshot.documents.mapNotNull { it.toObject(RestaurantTable::class.java)?.copy(id = it.id) }
                _tables.value = tableList
            }
        }
    }

    private fun listenToMenuItems() {
        db.collection("menu_items").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { it.toObject(MenuItem::class.java)?.copy(id = it.id) }
                _menuItems.value = items
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

    fun updateTableStatus(tableId: String, status: String) {
        db.collection("tables").document(tableId).update("status", status)
    }

    fun createOrderForTable(tableId: String) {
        val newOrder = Order(tableId = tableId, status = "Open")
        db.collection("orders").add(newOrder).addOnSuccessListener { 
            updateTableStatus(tableId, "Đang phục vụ")
        }
    }

    fun addMenuItemToOrder(orderId: String, menuItem: MenuItem) {
        // Optimistic local update for instant UI feedback
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
            
            // Lấy các món trong giỏ hàng để gửi xuống bếp
            val cartItems = order.items.filter { it.status == "Cart" }
            
            if (cartItems.isEmpty()) return@addOnSuccessListener
            
            // Cập nhật đơn hàng hiện tại: chuyển Cart -> Pending (lưu vào hóa đơn)
            val updatedItems = order.items.map { 
                if (it.status == "Cart") it.copy(status = "Pending") else it 
            }
            
            // Cập nhật đơn hàng hiện tại với các món đã gửi
            db.collection("orders").document(orderId).update(
                mapOf(
                    "items" to updatedItems,
                    "totalAmount" to order.totalAmount
                )
            ).addOnSuccessListener {
                // Tạo đơn hàng mới cho lượt gọi tiếp theo (giỏ hàng trống)
                val newOrder = Order(
                    tableId = order.tableId,
                    status = "Open",
                    items = emptyList(),
                    totalAmount = 0.0
                )
                db.collection("orders").add(newOrder)
            }
        }
    }

    fun removeOrderItem(orderId: String, menuItemId: String) {
        // Optimistic local update for instant UI feedback
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
