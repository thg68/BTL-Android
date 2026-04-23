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
    }

    private fun listenToTables() {
        db.collection("tables").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("PosViewModel", "Listen failed.", e)
                return@addSnapshotListener
            }
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
        // Simple append, in reality we'd check if it exists and increment quantity
        db.collection("orders").document(orderId).get().addOnSuccessListener { doc ->
            val order = doc.toObject(Order::class.java)
            if (order != null) {
                val newItem = OrderItem(
                    menuItemId = menuItem.id,
                    name = menuItem.name,
                    quantity = 1,
                    price = menuItem.price,
                    status = "Pending"
                )
                val updatedItems = order.items + newItem
                val newTotal = order.totalAmount + newItem.price
                db.collection("orders").document(orderId).update(
                    "items", updatedItems,
                    "totalAmount", newTotal
                )
            }
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
}
