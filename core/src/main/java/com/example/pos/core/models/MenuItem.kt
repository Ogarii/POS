package com.example.pos.core.models

import com.example.pos.core.money.Money

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val price: Money,
    val isAvailable: Boolean = true
)
