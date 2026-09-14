package com.example.pos.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pos.nav.Category
import com.example.pos.nav.MenuCatalog
import com.example.pos.core.logic.CartManager
import com.example.pos.core.logic.OrderCalculator
import com.example.pos.core.models.MenuItem
import com.example.pos.core.models.CartLine
import com.example.pos.core.models.CartSnapshot
import com.example.pos.core.models.Discount
import com.example.pos.core.models.OrderTotals
import com.example.pos.ui.theme.POSTheme
import com.example.pos.core.money.Money
import java.math.BigDecimal
import java.util.UUID



@Composable
fun TakeOrderScreen(
    categories: List<Category>,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSubmitOrder: (List<CartLine>) -> Unit = {}
) {
    val cartManager = remember { CartManager() }
    val cartSnapshot by cartManager.snapshot.collectAsState()
    
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
    var cartExpanded by remember { mutableStateOf(false) }
    var showTenderDialog by remember { mutableStateOf(false) }
    
    val discounts = listOf(
        Discount.Percentage("None", 0),
        Discount.Percentage("5%", 5),
        Discount.Percentage("10%", 10),
        Discount.Percentage("20%", 20)
    )
    var selectedDiscount by remember { mutableStateOf(discounts[0]) }

    val filteredItems = remember(menuItems, selectedCategoryId) {
        if (selectedCategoryId == null) menuItems
        else menuItems.filter { it.category == selectedCategoryId }
    }

    val orderTotals = remember(cartSnapshot, selectedDiscount) {
        OrderCalculator.calculate(cartSnapshot, selectedDiscount)
    }

    Column(modifier = modifier.fillMaxSize().statusBarsPadding()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Take Order", style = MaterialTheme.typography.headlineSmall)
        }

        //categories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("All") }
                )
            }
            items(categories, key = { it.id }) { category ->
                FilterChip(
                    selected = category.id == selectedCategoryId,
                    onClick = { selectedCategoryId = category.id },
                    label = { Text(category.label) }
                )
            }
        }

        HorizontalDivider()

        //Menu Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                MenuItemCard(item = item, onClick = { cartManager.addItem(item) })
            }
        }

        HorizontalDivider()


        //Discounts
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Discount", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                discounts.forEach { discount ->
                    FilterChip(
                        selected = selectedDiscount == discount,
                        onClick = { selectedDiscount = discount },
                        label = { Text(discount.label) }
                    )
                }
            }
        }

        HorizontalDivider()

        //Cart Summary
        CartSummaryBar(
            itemCount = cartSnapshot.itemCount,
            total = orderTotals.total,
            expanded = cartExpanded,
            onToggle = { cartExpanded = !cartExpanded }
        )

        //Expand Cart
        if (cartExpanded) {
            CartDetail(
                lines = cartSnapshot.lines,
                total = orderTotals.total,
                onIncrease = { lineId -> cartManager.updateQuantity(lineId, +1) },
                onDecrease = { lineId -> cartManager.updateQuantity(lineId, -1) },
                onCharge = { showTenderDialog = true }
            )
        }
    }

    if (showTenderDialog) {
        TenderDialog(
            total = orderTotals.total,
            onDismiss = { showTenderDialog = false },
            onConfirm = {
                onSubmitOrder(cartSnapshot.lines)
                cartManager.clear()
                showTenderDialog = false
                cartExpanded = false
            }
        )
    }
}

@Composable
private fun TenderDialog(
    total: Money,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var amountTenderedStr by remember { mutableStateOf("") }
    val amountTendered = Money.parseDecimalString(amountTenderedStr)
    val changeDue = if (amountTendered >= total) amountTendered - total else Money.ZERO

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tender Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Due:", style = MaterialTheme.typography.bodyLarge)
                    Text(total.format(), fontWeight = FontWeight.Bold)
                }
                
                OutlinedTextField(
                    value = amountTenderedStr,
                    onValueChange = { amountTenderedStr = it },
                    label = { Text("Amount Tendered") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Change Due:", style = MaterialTheme.typography.bodyLarge)
                    Text(changeDue.format(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = amountTendered >= total && amountTenderedStr.isNotEmpty()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.price.format(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun CartSummaryBar(
    itemCount: Int,
    total: Money,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onToggle() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (itemCount == 0) "Cart is empty" else "$itemCount item${if (itemCount != 1) "s" else ""} in cart",
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(total.format(), fontWeight = FontWeight.Bold)
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = if (expanded) "Collapse cart" else "Expand cart"
            )
        }
    }
}

@Composable
private fun CartDetail(
    lines: List<CartLine>,
    total: Money,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onCharge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
    ) {
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(lines, key = { it.id }) { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(line.item.name, modifier = Modifier.weight(1f))
                            Text(
                                line.total.format(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (!line.note.isNullOrBlank()) {
                            Text(line.note!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        IconButton(onClick = { onDecrease(line.id) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(line.quantity.toString())
                        IconButton(onClick = { onIncrease(line.id) }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                HorizontalDivider()
            }
        }

        Button(
            onClick = onCharge,
            enabled = lines.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Charge ${total.format()}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TakeOrderScreenPreview() {
    POSTheme {
        TakeOrderScreen(
            categories = MenuCatalog.categories,
            menuItems = MenuCatalog.menuItems,
            onBack = {},
            onSubmitOrder = {
                TODO("initiate printing and creation of order")
            }
        )
    }
}