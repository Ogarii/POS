package com.example.pos.nav

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.pos.nav.ui.theme.POSTheme
import com.example.pos.core.models.*
import com.example.pos.core.receipt.ReceiptRenderer
import com.example.pos.core.money.Money
import com.example.pos.orders.OrderStore
import com.example.pos.config.BluetoothPrinterService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            POSTheme {
                ProductScreenContent()

            }
        }
    }
}

@Composable
fun ProductScreenContent(modifier: Modifier = Modifier) {
    val orders = OrderStore.orders
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hasBluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Orders",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No orders yet", color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onPay = { OrderStore.markPaid(order.id) },
                        onPrint = {
                            if (!hasBluetoothPermission) return@OrderCard
                            val printerService = BluetoothPrinterService(context)
                            val renderer = ReceiptRenderer(32)
                            
                            val receiptData = ReceiptData(
                                merchantInfo = MerchantInfo(
                                    name = "MAMA'S KITCHEN",
                                    address = "123 Bishara Street, Nairobi",
                                    tel = "0700 000 000",
                                    pin = "P051234567X"
                                ),
                                receiptMeta = ReceiptMeta(
                                    number = order.id,
                                    cashier = "JANE",
                                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                                ),
                                lines = order.lines.map { line ->
                                    ReceiptProductLine(
                                        name = line.item.name,
                                        quantity = line.quantity,
                                        price = line.item.price,
                                        total = line.total
                                    )
                                },
                                totals = ReceiptTotals(
                                    subtotal = order.total,
                                    discount = Money.ZERO,
                                    taxes = Money.ZERO,
                                    total = order.total
                                ),
                                payment = ReceiptPayment(
                                    method = "CASH",
                                    amount = order.total,
                                    change = Money.ZERO
                                ),
                                footerLines = listOf("Thank you for your business!", "Powered by Rack POS")
                            )

                            val receipt = renderer.render(receiptData)

                            scope.launch {
                                @Suppress("MissingPermission")
                                if (printerService.connectToFirstAvailablePrinter()) {
                                    printerService.print(receipt)
                                }
                            }
                        },
                        onRemove = { OrderStore.removeOrder(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    onPay: () -> Unit,
    onPrint: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFD7D7D7), RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = order.id, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${order.lines.sumOf { it.quantity }} item${if (order.lines.sumOf { it.quantity } != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(text = order.total.format(), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (order.status == OrderStatus.PAID) "Paid" else "Pending",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (order.status == OrderStatus.PAID) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrint,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Print", fontSize = 12.sp, maxLines = 1)
                }
                Button(
                    onClick = onPay,
                    enabled = order.status == OrderStatus.PENDING,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    val label = if (order.status == OrderStatus.PAID) "Paid" else "Pay"
                    Text(label, fontSize = 12.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(text = "Remove", fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductScreenPreview() {
    POSTheme {
        ProductScreenContent()
    }
}