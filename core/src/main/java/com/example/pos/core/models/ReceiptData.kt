package com.example.pos.core.models

import com.example.pos.core.money.Money

data class ReceiptData(
    val merchantInfo: MerchantInfo,
    val receiptMeta: ReceiptMeta,
    val lines: List<ReceiptProductLine>,
    val totals: ReceiptTotals,
    val payment: ReceiptPayment,
    val footerLines: List<String>
)

data class MerchantInfo(
    val name: String,
    val address: String,
    val tel: String,
    val pin: String
)

data class ReceiptMeta(
    val number: String,
    val cashier: String,
    val timestamp: String
)

data class ReceiptProductLine(
    val name: String,
    val quantity: Int,
    val price: Money,
    val total: Money
)

data class ReceiptTotals(
    val subtotal: Money,
    val discount: Money,
    val taxes: Money,
    val total: Money
)

data class ReceiptPayment(
    val method: String,
    val amount: Money,
    val change: Money
)
