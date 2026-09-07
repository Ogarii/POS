package com.example.pos.core

import com.example.pos.core.money.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class MoneyTest {
    @Test
    fun testArithmetic() {
        val m1 = Money.fromMajor(100.0)
        val m2 = Money.fromMajor(50.0)
        assertEquals(Money.fromMajor(150.0), m1 + m2)
        assertEquals(Money.fromMajor(50.0), m1 - m2)
        assertEquals(Money.fromMajor(200.0), m1 * 2)
    }

    @Test
    fun testFormatting() {
        val m = Money.fromMajor(1234.56)
        val formatted = m.format(Locale("en", "KE"))
        // Check if it contains the numerical value formatted correctly
        assertTrue("Formatted string '$formatted' should contain '1,234.56'", formatted.contains("1,234.56"))
    }
}
