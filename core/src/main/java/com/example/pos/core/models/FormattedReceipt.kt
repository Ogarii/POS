package com.example.pos.core.models

data class FormattedReceipt(
    val columnCount: Int,
    val lines: List<PrintLine>
) {
    fun toPlainText(): String {
        return lines.joinToString("\n") { line ->
            when (line) {
                is PrintLine.Text -> {
                    val content = line.content
                    when (line.alignment) {
                        PrintLine.Alignment.LEFT -> content.padEnd(columnCount)
                        PrintLine.Alignment.RIGHT -> content.padStart(columnCount)
                        PrintLine.Alignment.CENTER -> {
                            val padding = (columnCount - content.length) / 2
                            " ".repeat(padding.coerceAtLeast(0)) + content + " ".repeat((columnCount - content.length - padding).coerceAtLeast(0))
                        }
                    }
                }
                PrintLine.Divider -> "-".repeat(columnCount)
                is PrintLine.Feed -> "\n".repeat(line.lines - 1)
                is PrintLine.Barcode -> "[Barcode: ${line.data}]"
                is PrintLine.QRCode -> "[QRCode: ${line.data}]"
                PrintLine.Cut -> "[CUT]"
            }
        }
    }
}
