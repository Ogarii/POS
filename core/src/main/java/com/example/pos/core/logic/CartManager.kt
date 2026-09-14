package com.example.pos.core.logic

import com.example.pos.core.models.*
import com.example.pos.core.money.Money
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class CartManager {
    private val _snapshot = MutableStateFlow(CartSnapshot(emptyList(), Money.ZERO))
    val snapshot: StateFlow<CartSnapshot> = _snapshot.asStateFlow()


    fun addItem(item: MenuItem, note: String? = null) {
        _snapshot.update { current ->
            val existingIndex = current.lines.indexOfFirst { it.item == item && it.note == note }
            val newLines = if (existingIndex != -1) {
                current.lines.mapIndexed { index, line ->
                    if (index == existingIndex) line.copy(quantity = line.quantity + 1) else line
                }
            } else {
                current.lines + CartLine(id = UUID.randomUUID().toString(), item = item, quantity = 1, note = note)
            }
            calculateSnapshot(newLines)
        }
    }

    fun updateQuantity(lineId: String, delta: Int) {
        _snapshot.update { current ->
            val newLines = current.lines.mapNotNull { line ->
                if (line.id == lineId) {
                    val newQty = line.quantity + delta
                    if (newQty > 0) line.copy(quantity = newQty) else null
                } else line
            }
            calculateSnapshot(newLines)
        }
    }

    fun removeLine(lineId: String) {
        _snapshot.update { current ->
            val newLines = current.lines.filter { it.id != lineId }
            calculateSnapshot(newLines)
        }
    }

    fun clear() {
        _snapshot.value = CartSnapshot(emptyList(), Money.ZERO)
    }

    private fun calculateSnapshot(lines: List<CartLine>): CartSnapshot {
        val subtotal = lines.fold(Money.ZERO) { acc, line -> acc + line.total }
        return CartSnapshot(lines, subtotal)
    }
}
