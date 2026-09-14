package com.example.pos.core.logic

import com.example.pos.core.models.*
import com.example.pos.core.money.Money
import java.math.BigDecimal
import java.math.RoundingMode

object OrderCalculator {
    fun calculate(snapshot: CartSnapshot, discount: Discount? = null): OrderTotals {
        val subtotal = snapshot.subtotal
        val discountAmount = discount?.calculate(subtotal) ?: Money.ZERO
        val taxableBase = subtotal - discountAmount
        
        // VAT 16% half-up
        val vatRate = BigDecimal("0.16")
        val vatAmount = taxableBase.multiply(vatRate, RoundingMode.HALF_UP)
        val taxLines = listOf(TaxLine("VAT 16%", vatAmount))
        
        val total = taxableBase + vatAmount
        
        return OrderTotals(
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxableBase = taxableBase,
            taxes = taxLines,
            total = total
        )
    }
}
