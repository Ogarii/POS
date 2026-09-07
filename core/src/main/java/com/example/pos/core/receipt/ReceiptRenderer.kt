package com.example.pos.core.receipt

import com.example.pos.core.models.*
import com.example.pos.core.money.Money

class ReceiptRenderer(private val columnCount: Int = 32) {
    fun render(data: ReceiptData): FormattedReceipt {
        val lines = mutableListOf<PrintLine>()

        //Merchant Info
        lines.add(PrintLine.Text(data.merchantInfo.name, PrintLine.Alignment.CENTER, isBold = true, scale = 2))
        lines.add(PrintLine.Text(data.merchantInfo.address, PrintLine.Alignment.CENTER))
        lines.add(PrintLine.Text("Tel: ${data.merchantInfo.tel}", PrintLine.Alignment.CENTER))
        lines.add(PrintLine.Text("PIN: ${data.merchantInfo.pin}", PrintLine.Alignment.CENTER))
        
        lines.add(PrintLine.Divider)

        //Meta Info
        lines.add(PrintLine.Text("Receipt: ${data.receiptMeta.number}"))
        lines.add(PrintLine.Text("Cashier: ${data.receiptMeta.cashier}"))
        lines.add(PrintLine.Text("Date: ${data.receiptMeta.timestamp}"))
        
        lines.add(PrintLine.Divider)

        //Product Lines
        data.lines.forEach { line ->
            val qtyStr = "${line.quantity} x ${line.price.format()}"
            val priceStr = line.total.format()
            val spacing = columnCount - qtyStr.length - priceStr.length
            
            lines.add(PrintLine.Text(line.name))
            if (spacing > 0) {
                lines.add(PrintLine.Text(qtyStr + " ".repeat(spacing) + priceStr))
            } else {
                lines.add(PrintLine.Text(qtyStr))
                lines.add(PrintLine.Text(priceStr, PrintLine.Alignment.RIGHT))
            }
        }

        lines.add(PrintLine.Divider)

        //Totals
        lines.add(renderKeyValue("SUBTOTAL", data.totals.subtotal))
        lines.add(renderKeyValue("DISCOUNT", data.totals.discount))
        lines.add(renderKeyValue("TAX", data.totals.taxes))
        lines.add(PrintLine.Text(renderKeyValueText("TOTAL", data.totals.total), isBold = true))

        lines.add(PrintLine.Divider)

        //Payment
        lines.add(PrintLine.Text("Payment Method: ${data.payment.method}"))
        lines.add(renderKeyValue("AMOUNT PAID", data.payment.amount))
        lines.add(renderKeyValue("CHANGE", data.payment.change))

        lines.add(PrintLine.Divider)

        //foote
        data.footerLines.forEach { footer ->
            lines.add(PrintLine.Text(footer, PrintLine.Alignment.CENTER))
        }
        
        lines.add(PrintLine.Feed(3))
        lines.add(PrintLine.Cut)

        return FormattedReceipt(columnCount, lines)
    }

    private fun renderKeyValue(key: String, value: Money): PrintLine.Text {
        return PrintLine.Text(renderKeyValueText(key, value))
    }

    private fun renderKeyValueText(key: String, value: Money): String {
        val valStr = value.format()
        val spacing = columnCount - key.length - valStr.length
        return key + " ".repeat(spacing.coerceAtLeast(1)) + valStr
    }
}
