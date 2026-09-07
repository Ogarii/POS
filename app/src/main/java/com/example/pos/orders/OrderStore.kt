package com.example.pos.orders

import androidx.compose.runtime.mutableStateListOf
import com.example.pos.core.models.Order
import com.example.pos.core.models.OrderStatus

object OrderStore {
    private val _orders = mutableStateListOf<Order>()
    val orders: List<Order> get() = _orders

    fun addOrder(order: Order) {
        _orders.add(0, order)
    }

    fun removeOrder(id: String) {
        _orders.removeAll { it.id == id }
    }

    fun markPaid(id: String) {
        val index = _orders.indexOfFirst { it.id == id }
        if (index != -1) {
            _orders[index] = _orders[index].copy(status = OrderStatus.PAID)
        }
    }
}