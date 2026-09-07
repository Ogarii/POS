package com.example.pos.core

import com.example.pos.core.models.Discount
import com.example.pos.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class DiscountTest {
    @Test
    fun testPercentageDiscount() {
        val discount = Discount.Percentage("10%", 10)
        val subtotal = Money.fromMajor(1000.0)
        val discountAmount = discount.calculate(subtotal)
        assertEquals(Money.fromMajor(100.0), discountAmount)
    }

    @Test
    fun testFixedDiscount() {
        val discount = Discount.Fixed("50 OFF", Money.fromMajor(50.0))
        val subtotal = Money.fromMajor(1000.0)
        val discountAmount = discount.calculate(subtotal)
        assertEquals(Money.fromMajor(50.0), discountAmount)
    }

    @Test
    fun testFixedDiscountCappedBySubtotal() {
        val discount = Discount.Fixed("1000 OFF", Money.fromMajor(1000.0))
        val subtotal = Money.fromMajor(500.0)
        val discountAmount = discount.calculate(subtotal)
        assertEquals(Money.fromMajor(500.0), discountAmount)
    }
}
