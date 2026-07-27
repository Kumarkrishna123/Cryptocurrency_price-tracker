package com.cryptocurrencyprice_tracker.ui.format

import java.util.Locale

fun formatPrice(price: Double): String = when {
    price >= 1.0 -> String.format(Locale.US, "$%,.2f", price)
    else -> String.format(Locale.US, "$%,.6f", price)
}

fun formatPercent(change: Double): String = String.format(Locale.US, "%+.2f%%", change)

fun formatCompactUsd(value: Double?): String {
    if (value == null) return "—"
    val abs = kotlin.math.abs(value)
    return when {
        abs >= 1_000_000_000_000 -> String.format(Locale.US, "$%.2fT", value / 1_000_000_000_000)
        abs >= 1_000_000_000 -> String.format(Locale.US, "$%.2fB", value / 1_000_000_000)
        abs >= 1_000_000 -> String.format(Locale.US, "$%.2fM", value / 1_000_000)
        abs >= 1_000 -> String.format(Locale.US, "$%.2fK", value / 1_000)
        else -> String.format(Locale.US, "$%,.2f", value)
    }
}

fun formatPriceOrDash(price: Double?): String = price?.let { formatPrice(it) } ?: "—"
