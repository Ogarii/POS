package com.example.pos.core.models

sealed class PrintLine {
    enum class Alignment { LEFT, CENTER, RIGHT }

    data class Text(
        val content: String,
        val alignment: Alignment = Alignment.LEFT,
        val isBold: Boolean = false,
        val scale: Int = 1
    ) : PrintLine()

    object Divider : PrintLine()
    
    data class Feed(val lines: Int) : PrintLine()
    
    data class Barcode(val data: String) : PrintLine()
    
    data class QRCode(val data: String) : PrintLine()
    
    object Cut : PrintLine()
}
