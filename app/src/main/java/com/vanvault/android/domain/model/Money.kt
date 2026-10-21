package com.vanvault.android.domain.model

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

/** Money is a Value Object — immutable, currency-aware, equality by value. */
@JvmInline
value class Money private constructor(val cents: Long) {

    fun toDisplayString(locale: Locale = Locale("es", "CO")): String {
        val format = NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
        }
        return format.format(cents / 100.0)
    }

    operator fun times(quantity: Int): Money =
        Money(cents * quantity)

    companion object {
        fun ofCop(whole: Long): Money = Money(whole * 100)
        fun fromDouble(value: Double): Money = Money((value * 100).roundToLong())
        val ZERO = Money(0)
    }
}