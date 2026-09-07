package com.example.pos.core.money

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

@JvmInline
value class Money(val minorUnits: Long) : Comparable<Money> {

    companion object {
        private const val DECIMALS = 2
        private val FACTOR = BigDecimal.TEN.pow(DECIMALS)

        val KES: Currency = Currency.getInstance("KES")
        val ZERO = Money(0L)

        fun fromMajor(amount: Double) = Money((amount * 100).toLong())

        fun parseDecimalString(s: String): Money {
            val amount = s.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
            return Money(amount.setScale(DECIMALS, RoundingMode.HALF_EVEN).movePointRight(DECIMALS).longValueExact())
        }
    }

    //calculations
    operator fun plus(other: Money) = Money(minorUnits + other.minorUnits)
    operator fun minus(other: Money) = Money(minorUnits - other.minorUnits)
    operator fun times(multiplier: Int) = Money(minorUnits * multiplier)
    operator fun unaryMinus() = Money(-minorUnits)

    override fun compareTo(other: Money) = minorUnits.compareTo(other.minorUnits)

    //Conventional Banking rounding
    fun multiply(rate: BigDecimal, rounding: RoundingMode = RoundingMode.HALF_EVEN): Money {
        val result = minorUnits.toBigDecimal().multiply(rate)
        return Money(result.setScale(0, rounding).longValueExact())
    }

    fun format(locale: Locale = Locale("en", "KE")): String {
        return NumberFormat.getCurrencyInstance(locale).apply {
            currency = KES
        }.format(minorUnits.toDouble() / 100)
    }
}