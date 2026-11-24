package com.partitionsoft.splitbillapp

import com.partitionsoft.splitbillapp.core.format.MoneyFormatter
import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyAndFormatterTest {

    @Test
    fun percentageHalfUpRounding() {
        // $10.01 * 15% = $1.5015 -> $1.50
        assertEquals(Money(150), Money(1001).percentageOfBasisPoints(1500))
        // $10.05 * 10% = $1.005 -> $1.01 (half-up)
        assertEquals(Money(101), Money(1005).percentageOfBasisPoints(1000))
        // $120.00 * 15% = $18.00
        assertEquals(Money(1800), Money(12000).percentageOfBasisPoints(1500))
        // $100.00 * 12.5% = $12.50
        assertEquals(Money(1250), Money(10000).percentageOfBasisPoints(1250))
    }

    @Test
    fun splitEvenlyPreservesExactTotal() {
        val shares = Money(10000).splitEvenly(3)
        assertEquals(listOf(Money(3334), Money(3333), Money(3333)), shares)
        assertEquals(10000L, shares.sumOf { it.minorUnits })

        val shares7 = Money(10000).splitEvenly(7)
        assertEquals(10000L, shares7.sumOf { it.minorUnits })
        assertEquals(7, shares7.size)
    }

    @Test
    fun currencyFormattingHandlesGroupingAndZeroDecimalCurrencies() {
        assertEquals("$1,234.50", MoneyFormatter.format(Money(123450), Currency.USD))
        assertEquals("$120", MoneyFormatter.format(Money(12000), Currency.USD, trimWholeZeros = true))
        assertEquals("€46.00", MoneyFormatter.format(Money(4600), Currency.EUR))
        assertEquals("1,500.25 ₴", MoneyFormatter.format(Money(150025), Currency.UAH))
        assertEquals("¥3,500", MoneyFormatter.format(Money(3500), Currency.JPY))
    }

    @Test
    fun percentageFormattingOmitsTrailingZeros() {
        assertEquals("15%", MoneyFormatter.formatPercentage(1500))
        assertEquals("12.5%", MoneyFormatter.formatPercentage(1250))
        assertEquals("18.75%", MoneyFormatter.formatPercentage(1875))
        assertEquals("0%", MoneyFormatter.formatPercentage(0))
    }
}
