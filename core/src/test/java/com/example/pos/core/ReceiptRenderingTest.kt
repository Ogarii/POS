package com.example.pos.core

import com.example.pos.core.models.*
import com.example.pos.core.money.Money
import com.example.pos.core.receipt.ReceiptRenderer
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptRenderingTest {

    @Test
    fun testReceiptRendering() {
        val data = ReceiptData(
            merchantInfo = MerchantInfo(
                name = "Best Coffee Shop",
                address = "123 Main St, City",
                tel = "555-0123",
                pin = "12345678"
            ),
            receiptMeta = ReceiptMeta(
                number = "REC-001",
                cashier = "John Doe",
                timestamp = "2023-10-27 10:30"
            ),
            lines = listOf(
                ReceiptProductLine("Latte", 2, Money.fromMajor(4.50), Money.fromMajor(9.00)),
                ReceiptProductLine("Croissant", 1, Money.fromMajor(3.25), Money.fromMajor(3.25))
            ),
            totals = ReceiptTotals(
                subtotal = Money.fromMajor(12.25),
                discount = Money.ZERO,
                taxes = Money.fromMajor(1.23),
                total = Money.fromMajor(13.48)
            ),
            payment = ReceiptPayment(
                method = "CASH",
                amount = Money.fromMajor(20.00),
                change = Money.fromMajor(6.52)
            ),
            footerLines = listOf("Thank you for your visit!", "Please come again")
        )

        val renderer = ReceiptRenderer(columnCount = 32)
        val formattedReceipt = renderer.render(data)
        val plainText = formattedReceipt.toPlainText()

        // Simple assertions to verify content presence
        assertTrue(plainText.contains("Best Coffee Shop"))
        assertTrue(plainText.contains("REC-001"))
        assertTrue(plainText.contains("Latte"))
        assertTrue(plainText.contains("TOTAL"))
        // Check for formatted money string (e.g. "13.48" part of it)
        assertTrue("Output should contain total 13.48", plainText.contains("13.48"))
        assertTrue(plainText.contains("Thank you"))
        assertTrue(plainText.contains("[CUT]"))
        
        println(plainText)
    }
}
