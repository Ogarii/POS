package com.example.pos.core.config

enum class PrinterStatus {
    READY,
    OUT_OF_PAPER,
    LOW_BATTERY,
    BUSY,
    HARDWARE_ERROR,
    DISCONNECTED
}

data class PrintResult(
    val status: PrinterStatus,
    val message: String
)
