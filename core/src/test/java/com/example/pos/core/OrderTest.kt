package com.example.pos.core

import com.example.pos.core.models.Order
import com.example.pos.core.models.CartLine
import com.example.pos.core.models.MenuItem
import com.example.pos.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class OrderTest {
    @Test
    fun testTotalCalculation() {
        val p1 = MenuItem("1", "Water", "Water", "drinks", Money.fromMajor(50.0))
        val p2 = MenuItem("2", "Bread", "Bread", "food", Money.fromMajor(60.0))
        
        val line1 = CartLine("l1", p1, 2) // 100
        val line2 = CartLine("l2", p2, 1) // 60
        
        val lines = listOf(line1, line2)
        val total = Order.calculateTotal(lines)
        
        assertEquals(Money.fromMajor(160.0), total)
        
        val order = Order("o1", lines, total, "2023-10-27T10:00:00")
        assertEquals(Money.fromMajor(160.0), order.total)
    }
}
