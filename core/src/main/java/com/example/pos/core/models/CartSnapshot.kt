package com.example.pos.core.models

import com.example.pos.core.money.Money

data class CartSnapshot(
    val lines: List<CartLine>,
    val subtotal: Money
) {
    val itemCount: Int = lines.sumOf { it.quantity }
}
