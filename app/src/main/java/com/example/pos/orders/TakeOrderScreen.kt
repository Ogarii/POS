package com.example.pos.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pos.nav.Category
import com.example.pos.nav.MenuCatalog
import com.example.pos.core.models.MenuItem
import com.example.pos.core.models.CartLine
import com.example.pos.core.models.CartSnapshot
import com.example.pos.core.models.Discount
import com.example.pos.ui.theme.POSTheme
import com.example.pos.core.money.Money
import java.util.UUID



@Composable
fun TakeOrderScreen(
    categories: List<Category>,
    menuItems: List<MenuItem>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSubmitOrder: (List<CartLine>) -> Unit = {}
) {
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
    var cartLines by remember { mutableStateOf(listOf<CartLine>()) }
    var cartExpanded by remember { mutableStateOf(false) }
    
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

    val cartSnapshot = remember(cartLines) {
        val subtotal = cartLines.fold(Money.ZERO) { acc, line -> acc + line.total }
        CartSnapshot(cartLines, subtotal)
    }

    
    val discountAmount = selectedDiscount.calculate(cartSnapshot.subtotal)
    val total = cartSnapshot.subtotal.minus(discountAmount)

    fun addToCart(item: MenuItem) {
        val existing = cartLines.find { it.item.id == item.id }
        cartLines = if (existing != null) {
            cartLines.map {
                if (it.item.id == item.id) it.copy(quantity = it.quantity + 1) else it
            }
        } else {
            cartLines + CartLine(id = UUID.randomUUID().toString(), item = item, quantity = 1)
        }
    }


    fun changeQuantity(itemId: String, delta: Int) {
        cartLines = cartLines
            .map { if (it.item.id == itemId) it.copy(quantity = it.quantity + delta) else it }
            .filter { it.quantity > 0 }
    }


    Column(modifier = modifier.fillMaxSize()) {

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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                MenuItemCard(item = item, onClick = { addToCart(item) })
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
            total = total,
            expanded = cartExpanded,
            onToggle = { cartExpanded = !cartExpanded }
        )

        //Expand Cart
        if (cartExpanded) {
            CartDetail(
                lines = cartLines,
                total = total,
                onIncrease = { itemId -> changeQuantity(itemId, +1) },
                onDecrease = { itemId -> changeQuantity(itemId, -1) },
                onSubmit = {
                    onSubmitOrder(cartLines)
                    cartLines = emptyList()
                    cartExpanded = false
                }
            )
        }
    }
}

@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text(item.price.format(), style = MaterialTheme.typography.bodyMedium)
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
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
    ) {
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(lines, key = { it.item.id }) { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(line.item.name)
                        Text(
                            line.total.format(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onDecrease(line.item.id) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(line.quantity.toString())
                        IconButton(onClick = { onIncrease(line.item.id) }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                HorizontalDivider()
            }
        }

        Button(
            onClick = onSubmit,
            enabled = lines.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Submit Order")
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