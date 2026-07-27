package com.cryptocurrencyprice_tracker.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun `shows two decimals for dollar-and-up prices`() {
        assertEquals("$65,000.00", formatPrice(65_000.0))
    }

    @Test
    fun `shows six decimals for sub-dollar coins`() {
        assertEquals("$0.000042", formatPrice(0.000042))
    }

    @Test
    fun `keeps the plus sign on gains`() {
        assertEquals("+1.50%", formatPercent(1.5))
        assertEquals("-2.30%", formatPercent(-2.3))
    }

    @Test
    fun `abbreviates large values so they fit a phone row`() {
        assertEquals("$1.28T", formatCompactUsd(1_280_000_000_000.0))
        assertEquals("$25.00B", formatCompactUsd(25_000_000_000.0))
        assertEquals("$3.40M", formatCompactUsd(3_400_000.0))
        assertEquals("$1.50K", formatCompactUsd(1_500.0))
    }

    @Test
    fun `renders unknown values as a dash rather than zero`() {
        assertEquals("—", formatCompactUsd(null))
        assertEquals("—", formatPriceOrDash(null))
    }
}
