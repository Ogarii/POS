package com.example.pos.core

import com.example.pos.core.logic.OrderCalculator
import com.example.pos.core.models.*
import com.example.pos.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class ScenarioTest {

    @Test
    fun testExactCase() {
        val chickenBurger = MenuItem("1", "Chicken Burger", "", "burgers", Money.fromMajor(450.0))
        val fries = MenuItem("2", "Fries", "", "sides", Money.fromMajor(200.0))
        val mangoJuice = MenuItem("3", "Fresh Mango Juice", "", "drinks", Money.fromMajor(180.0))

        val lines = listOf(
            CartLine("L1", chickenBurger, 2),
            CartLine("L2", fries, 1),
            CartLine("L3", mangoJuice, 3)
        )

        val subtotal = lines.fold(Money.ZERO) { acc, line -> acc + line.total }
        assertEquals(Money.fromMajor(1640.0), subtotal)

        val snapshot = CartSnapshot(lines, subtotal)
        val discount = Discount.Percentage("10%", 10)

        val totals = OrderCalculator.calculate(snapshot, discount)

        //1 Subtotal 1,640.00
        assertEquals(Money.fromMajor(1640.0), totals.subtotal)
        
        //2 Discount 10% = 164.00
        assertEquals(Money.fromMajor(164.0), totals.discountAmount)
        
        //3 Taxable base = 1,476.00
        assertEquals(Money.fromMajor(1476.0), totals.taxableBase)
        
        //4 VAT 16% - 1476.00 * 0.16 = 236.16
        assertEquals(Money.fromMajor(236.16), totals.taxes[0].amount)
        
        //5 TOTAL - 1476.00 + 236.16 = 1,712.16
        assertEquals(Money.fromMajor(1712.16), totals.total)

        //6 Cash tendered 2,000.00, Change 287.84
        val tendered = Money.fromMajor(2000.0)
        val change = tendered - totals.total
        assertEquals(Money.fromMajor(287.84), change)
    }
}
