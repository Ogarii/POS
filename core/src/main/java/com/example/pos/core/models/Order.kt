package com.example.pos.core.models

import com.example.pos.core.money.Money

data class Order(
    val id: String,
    val lines: List<CartLine>,
    val total: Money,
    val timestamp: String,
    val status: OrderStatus = OrderStatus.PENDING
) {
    companion object {
        fun calculateTotal(lines: List<CartLine>): Money {
            return lines.fold(Money.ZERO) { acc, line ->
                acc + (line.item.price * line.quantity)
            }
        }
    }
}
