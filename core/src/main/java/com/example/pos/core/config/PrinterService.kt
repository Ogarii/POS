package com.example.pos.core.config

import com.example.pos.core.models.FormattedReceipt

interface PrinterService {
    suspend fun getStatus(): PrinterStatus
    suspend fun print(receipt: FormattedReceipt): PrintResult
}
