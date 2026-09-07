package com.example.pos.core.models

import com.example.pos.core.money.Money
import java.math.BigDecimal
import java.math.RoundingMode

sealed class Discount {
    abstract val label: String
    abstract fun calculate(subtotal: Money): Money

    data class Percentage(override val label: String, val percentage: Int) : Discount() {
        override fun calculate(subtotal: Money): Money {
            val rate = BigDecimal(percentage).divide(BigDecimal(100))
            return subtotal.multiply(rate, RoundingMode.HALF_EVEN)
        }
    }

    data class Fixed(override val label: String, val amount: Money) : Discount() {
        override fun calculate(subtotal: Money): Money {
            return if (amount > subtotal) subtotal else amount
        }
    }
}
