package com.example.pos.core.models

data class CartLine(
    val id: String,
    val item: MenuItem,
    val quantity: Int,
    val note: String? = null
) {
    val total get() = item.price * quantity
}
