package com.example.pos.config

import com.example.pos.core.config.PrinterService
import com.example.pos.core.config.PrinterStatus
import com.example.pos.core.config.PrintResult
import com.example.pos.core.models.FormattedReceipt
import com.example.pos.core.models.PrintLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinterService(
    private val context: Context,
    private val receiptWidth: Int = 32,
) : PrinterService {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    override suspend fun getStatus(): PrinterStatus = withContext(Dispatchers.IO) {
        if (!hasPermissions()) return@withContext PrinterStatus.DISCONNECTED
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return@withContext PrinterStatus.DISCONNECTED
        if (bluetoothSocket?.isConnected == true) PrinterStatus.READY else PrinterStatus.DISCONNECTED
    }

    override suspend fun print(receipt: FormattedReceipt): PrintResult = withContext(Dispatchers.IO) {
        val out = outputStream ?: return@withContext PrintResult(PrinterStatus.DISCONNECTED, "Printer not connected")
        
        try {
            receipt.lines.forEach { line ->
                when (line) {
                    is PrintLine.Text -> {
                        when (line.alignment) {
                            PrintLine.Alignment.LEFT -> out.write(ESC_ALIGN_LEFT)
                            PrintLine.Alignment.CENTER -> out.write(ESC_ALIGN_CENTER)
                            PrintLine.Alignment.RIGHT -> out.write(ESC_ALIGN_RIGHT)
                        }
                        out.write(if (line.isBold) ESC_BOLD_ON else ESC_BOLD_OFF)
                        if (line.scale > 1) out.write(ESC_DOUBLE_HEIGHT_ON) else out.write(ESC_NORMAL_SIZE)
                        out.write("${line.content}\n".toByteArray())
                    }
                    PrintLine.Divider -> {
                        out.write(ESC_ALIGN_LEFT)
                        out.write(ESC_BOLD_OFF)
                        out.write(ESC_NORMAL_SIZE)
                        out.write(("-".repeat(receipt.columnCount) + "\n").toByteArray())
                    }
                    is PrintLine.Feed -> {
                        repeat(line.lines) { out.write(LINE_FEED) }
                    }
                    is PrintLine.Barcode -> {
                        out.write(ESC_ALIGN_CENTER)
                        out.write(byteArrayOf(0x1D, 0x6B, 0x04)) // CODE39
                        out.write(line.data.toByteArray())
                        out.write(0x00.toByte().toInt())
                        out.write(LINE_FEED)
                    }
                    is PrintLine.QRCode -> {
                        printQRCodeInternal(out, line.data)
                    }
                    PrintLine.Cut -> {
                        out.write(CUT_PAPER)
                    }
                }
            }
            out.flush()
            PrintResult(PrinterStatus.READY, "Print successful")
        } catch (e: IOException) {
            PrintResult(PrinterStatus.HARDWARE_ERROR, "IO Error: ${e.message}")
        }
    }

    private fun printQRCodeInternal(out: OutputStream, data: String) {
        try {
            val qrSize = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x04)
            val qrError = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31)
            val storeLength = data.length + 3
            val qrStore = byteArrayOf(0x1D, 0x28, 0x6B, (storeLength % 256).toByte(), (storeLength / 256).toByte(), 0x31, 0x50, 0x30)
            val qrPrint = byteArrayOf(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30)

            out.write(ESC_ALIGN_CENTER)
            out.write(qrSize)
            out.write(qrError)
            out.write(qrStore)
            out.write(data.toByteArray(Charsets.UTF_8))
            out.write(qrPrint)
            out.write(LINE_FEED)
        } catch (_: Exception) {}
    }

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions(activity: Activity, requestCode: Int) {
        if (!hasPermissions()) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
            } else {
                arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
            }
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun connectByMacAddress(macAddress: String): Boolean {
        if (!hasPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false
        return try {
            val printer = bluetoothAdapter.getRemoteDevice(macAddress) ?: return false
            bluetoothAdapter.cancelDiscovery()
            return connectToDevice(printer)
        } catch (e: Exception) {
            closeCurrentConnection()
            e.printStackTrace()
            false
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun connectToDefaultPrinter(): Boolean {
        if (!hasPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false
        bluetoothAdapter.cancelDiscovery()

        val devices = bluetoothAdapter.bondedDevices.toList()
        for (device in devices) {
            if (connectToDevice(device)) return true
        }

        return false
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    private fun connectToDevice(device: BluetoothDevice): Boolean {
        return try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (_: IOException) {
            closeCurrentConnection()
            try {
                bluetoothSocket = device.javaClass
                    .getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    .invoke(device, 1) as BluetoothSocket
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                true
            } catch (_: Exception) {
                closeCurrentConnection()
                false
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun connect(printerName: String): Boolean {
        if (!hasPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false
        bluetoothAdapter.cancelDiscovery()
        val printer = bluetoothAdapter.bondedDevices.firstOrNull { it.name?.contains(printerName) == true } ?: return false
        return try {
            bluetoothSocket = printer.createRfcommSocketToServiceRecord(MY_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            true
        } catch (e: IOException) {
            closeCurrentConnection()
            try {
                bluetoothSocket = printer.javaClass
                    .getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    .invoke(printer, 1) as BluetoothSocket
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                true
            } catch (e2: Exception) {
                closeCurrentConnection()
                e2.printStackTrace()
                false
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    fun connectToFirstAvailablePrinter(): Boolean {
        if (!hasPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return false
        bluetoothAdapter.cancelDiscovery()
        val pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.isNullOrEmpty()) return false

        pairedDevices.forEach { device ->
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                return true
            } catch (_: IOException) {
                closeCurrentConnection()
                try {
                    bluetoothSocket = device.javaClass
                        .getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                        .invoke(device, 1) as BluetoothSocket
                    bluetoothSocket?.connect()
                    outputStream = bluetoothSocket?.outputStream
                    return true
                } catch (_: Exception) {
                    closeCurrentConnection()
                }
            }
        }
        return false
    }

    fun disconnect() {
        closeCurrentConnection()
    }

    private fun closeCurrentConnection() {
        try {
            outputStream?.close()
            outputStream = null
            bluetoothSocket?.close()
            bluetoothSocket = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isConnected(): Boolean = bluetoothSocket?.isConnected == true

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val pairedDevices: List<String>
        get() {
            val deviceList = mutableListOf<String>()
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                bluetoothAdapter.bondedDevices.forEach { if (it.name != null) deviceList.add(it.name) }
            }
            return deviceList
        }

    @get:RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    val diagnosticInfo: String
        get() {
            if (bluetoothAdapter == null) return "Bluetooth adapter not available"
            if (!bluetoothAdapter.isEnabled) return "Bluetooth is disabled"
            if (!hasPermissions()) return "Bluetooth permissions not granted"
            val pairedDevices = bluetoothAdapter.bondedDevices
            if (pairedDevices.isNullOrEmpty()) return "No paired devices found"

            val info = StringBuilder()
            info.append("Paired devices (").append(pairedDevices.size).append("):\n")
            pairedDevices.forEach { device ->
                info.append("- ").append(device.name ?: "Unnamed device").append("\n")
            }
            return info.toString()
        }

    companion object {
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private val ESC_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
        private val ESC_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
        private val ESC_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)
        private val ESC_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        private val ESC_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
        private val ESC_DOUBLE_HEIGHT_ON = byteArrayOf(0x1B, 0x21, 0x10)
        private val ESC_NORMAL_SIZE = byteArrayOf(0x1B, 0x21, 0x00)
        private val LINE_FEED = byteArrayOf(0x0A)
        private val CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x00)
    }
}
