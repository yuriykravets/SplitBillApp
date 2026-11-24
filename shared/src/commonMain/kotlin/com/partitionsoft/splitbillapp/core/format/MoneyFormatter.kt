package com.partitionsoft.splitbillapp.core.format

import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money
import kotlin.math.abs

object MoneyFormatter {

    /**
     * Formats [money] with the given [currency], using grouping separators and
     * omitting trailing `.00` only when [trimWholeZeros] is true.
     */
    fun format(
        money: Money,
        currency: Currency,
        trimWholeZeros: Boolean = false,
    ): String {
        val isNegative = money.minorUnits < 0L
        val absMinor = abs(money.minorUnits)
        val numericText = formatNumeric(
            absMinorUnits = absMinor,
            fractionDigits = currency.fractionDigits,
            trimWholeZeros = trimWholeZeros,
        )
        val sign = if (isNegative) "-" else ""
        return if (currency.symbolAfterAmount) {
            "$sign$numericText ${currency.symbol}"
        } else {
            val separator = if (currency.symbol.length > 2) " " else ""
            "$sign${currency.symbol}$separator$numericText"
        }
    }

    /**
     * Formats [money] into an editable raw numeric string without currency symbol or commas
     * (e.g., "120.50" or "120").
     */
    fun formatEditable(money: Money, currency: Currency): String {
        if (money.minorUnits == 0L) return ""
        val factor = currency.minorUnitsPerMajor
        val whole = money.minorUnits / factor
        if (currency.fractionDigits == 0) return whole.toString()
        val frac = abs(money.minorUnits % factor)
        return if (frac == 0L) {
            whole.toString()
        } else {
            val padded = frac.toString().padStart(currency.fractionDigits, '0').trimEnd('0')
            "$whole.$padded"
        }
    }

    /**
     * Formats a tip expressed in basis points (e.g., 1500 -> "15%", 1250 -> "12.5%").
     */
    fun formatPercentage(basisPoints: Int): String {
        val whole = basisPoints / 100
        val frac = basisPoints % 100
        return if (frac == 0) {
            "$whole%"
        } else {
            val fracText = frac.toString().padStart(2, '0').trimEnd('0')
            "$whole.$fracText%"
        }
    }

    /**
     * Formats basis points as an editable percentage string without `%` (e.g., 1250 -> "12.5").
     */
    fun formatEditablePercentage(basisPoints: Int): String {
        val whole = basisPoints / 100
        val frac = basisPoints % 100
        return if (frac == 0) {
            whole.toString()
        } else {
            val fracText = frac.toString().padStart(2, '0').trimEnd('0')
            "$whole.$fracText"
        }
    }

    private fun formatNumeric(
        absMinorUnits: Long,
        fractionDigits: Int,
        trimWholeZeros: Boolean,
    ): String {
        var factor = 1L
        repeat(fractionDigits) { factor *= 10L }
        val wholePart = absMinorUnits / factor
        val groupedWhole = groupThousands(wholePart.toString())

        if (fractionDigits == 0) return groupedWhole

        val fractionalPart = absMinorUnits % factor
        if (trimWholeZeros && fractionalPart == 0L) return groupedWhole

        val paddedFraction = fractionalPart.toString().padStart(fractionDigits, '0')
        return "$groupedWhole.$paddedFraction"
    }

    private fun groupThousands(digits: String): String {
        if (digits.length <= 3) return digits
        val sb = StringBuilder(digits.length + digits.length / 3)
        val firstGroupSize = digits.length % 3
        if (firstGroupSize > 0) {
            sb.append(digits, 0, firstGroupSize)
            if (digits.length > firstGroupSize) sb.append(',')
        }
        var index = firstGroupSize
        while (index < digits.length) {
            sb.append(digits, index, index + 3)
            index += 3
            if (index < digits.length) sb.append(',')
        }
        return sb.toString()
    }
}
