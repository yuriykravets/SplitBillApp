package com.partitionsoft.splitbillapp.domain.usecase

import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money
import com.partitionsoft.splitbillapp.domain.model.ValidationError

sealed interface ParseMoneyResult {
    data class Valid(val money: Money) : ParseMoneyResult
    data class Invalid(val error: ValidationError) : ParseMoneyResult
}

sealed interface ParseTipResult {
    data class Valid(val basisPoints: Int) : ParseTipResult
    data class Invalid(val error: ValidationError) : ParseTipResult
}

/**
 * Pure Kotlin parser for user-entered decimal money amounts and custom tip percentages.
 * Handles both `.` and `,` decimal separators, trims spaces, rejects negative/malformed inputs,
 * and converts directly to exact integer minor units / basis points without floating-point drift.
 */
class ParseInputUseCase {

    fun parseBillAmount(raw: String, currency: Currency): ParseMoneyResult {
        val cleaned = raw.trim().replace(" ", "")
        if (cleaned.isEmpty()) {
            return ParseMoneyResult.Invalid(ValidationError.EmptyBillAmount)
        }
        return parseDecimalToMoney(
            cleaned = cleaned,
            currency = currency,
            requirePositive = true,
            invalidError = ValidationError.InvalidBillAmount,
            negativeError = ValidationError.NonPositiveBillAmount,
        )
    }

    fun parseAdditionalCharge(raw: String, currency: Currency): ParseMoneyResult {
        val cleaned = raw.trim().replace(" ", "")
        if (cleaned.isEmpty()) {
            return ParseMoneyResult.Valid(Money.ZERO)
        }
        return parseDecimalToMoney(
            cleaned = cleaned,
            currency = currency,
            requirePositive = false,
            invalidError = ValidationError.InvalidAdditionalCharge,
            negativeError = ValidationError.NegativeAdditionalCharge,
        )
    }

    /**
     * Parses a percentage string (e.g. "15", "12.5", "18,75") into basis points (1500, 1250, 1875).
     */
    fun parseTipPercentage(raw: String): ParseTipResult {
        val cleaned = raw.trim().replace(" ", "").replace(',', '.')
        if (cleaned.isEmpty()) {
            return ParseTipResult.Valid(0)
        }
        if (cleaned.startsWith("-")) {
            return ParseTipResult.Invalid(ValidationError.TipPercentageOutOfRange)
        }
        val parts = cleaned.split('.')
        if (parts.size > 2) {
            return ParseTipResult.Invalid(ValidationError.InvalidTipPercentage)
        }
        val wholePartStr = parts[0].ifEmpty { "0" }
        val fracPartStr = parts.getOrNull(1) ?: ""

        if (!wholePartStr.all(Char::isDigit) || !fracPartStr.all(Char::isDigit)) {
            return ParseTipResult.Invalid(ValidationError.InvalidTipPercentage)
        }
        if (fracPartStr.length > 2) {
            return ParseTipResult.Invalid(ValidationError.InvalidTipPercentage)
        }

        val whole = wholePartStr.toIntOrNull()
            ?: return ParseTipResult.Invalid(ValidationError.TipPercentageOutOfRange)
        val frac = fracPartStr.padEnd(2, '0').toIntOrNull() ?: 0
        val basisPoints = whole * 100 + frac

        if (basisPoints < 0 || basisPoints > MAX_TIP_BASIS_POINTS) {
            return ParseTipResult.Invalid(ValidationError.TipPercentageOutOfRange)
        }
        return ParseTipResult.Valid(basisPoints)
    }

    private fun parseDecimalToMoney(
        cleaned: String,
        currency: Currency,
        requirePositive: Boolean,
        invalidError: ValidationError,
        negativeError: ValidationError,
    ): ParseMoneyResult {
        if (cleaned.startsWith("-")) {
            return ParseMoneyResult.Invalid(negativeError)
        }
        val normalized = cleaned.replace(',', '.')
        val parts = normalized.split('.')
        if (parts.size > 2) {
            return ParseMoneyResult.Invalid(invalidError)
        }
        val wholePartStr = parts[0].ifEmpty { "0" }
        val fracPartStr = parts.getOrNull(1) ?: ""

        if (!wholePartStr.all(Char::isDigit) || !fracPartStr.all(Char::isDigit)) {
            return ParseMoneyResult.Invalid(invalidError)
        }
        if (currency.fractionDigits == 0 && fracPartStr.isNotEmpty()) {
            return ParseMoneyResult.Invalid(invalidError)
        }
        if (fracPartStr.length > currency.fractionDigits) {
            return ParseMoneyResult.Invalid(invalidError)
        }
        if (wholePartStr.length > MAX_WHOLE_DIGITS) {
            return ParseMoneyResult.Invalid(ValidationError.AmountTooLarge)
        }

        val whole = wholePartStr.toLongOrNull()
            ?: return ParseMoneyResult.Invalid(ValidationError.AmountTooLarge)
        val frac = if (currency.fractionDigits == 0) {
            0L
        } else {
            fracPartStr.padEnd(currency.fractionDigits, '0').toLongOrNull()
                ?: return ParseMoneyResult.Invalid(invalidError)
        }

        val minorUnits = whole * currency.minorUnitsPerMajor + frac
        if (minorUnits > MAX_MINOR_UNITS) {
            return ParseMoneyResult.Invalid(ValidationError.AmountTooLarge)
        }
        if (requirePositive && minorUnits <= 0L) {
            return ParseMoneyResult.Invalid(ValidationError.NonPositiveBillAmount)
        }
        return ParseMoneyResult.Valid(Money(minorUnits))
    }

    companion object {
        const val MAX_TIP_BASIS_POINTS = 20_000 // 200%
        private const val MAX_WHOLE_DIGITS = 9
        private const val MAX_MINOR_UNITS = 99_999_999_999L
    }
}
