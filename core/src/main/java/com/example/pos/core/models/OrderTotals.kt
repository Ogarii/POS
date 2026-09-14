package com.example.pos.core.models

import com.example.pos.core.money.Money

data class TaxLine(val label: String, val amount: Money)

data class OrderTotals(
    val subtotal: Money,
    val discountAmount: Money,
    val taxableBase: Money,
    val taxes: List<TaxLine>,
    val total: Money
)
