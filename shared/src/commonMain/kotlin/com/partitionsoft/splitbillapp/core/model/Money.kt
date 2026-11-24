package com.partitionsoft.splitbillapp.core.model

import kotlin.jvm.JvmInline

/**
 * Exact monetary representation stored as integer minor units (e.g., cents for USD/EUR, yen for JPY).
 * Avoids IEEE-754 floating-point rounding drift in financial calculations.
 */
@JvmInline
value class Money(val minorUnits: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(minorUnits + other.minorUnits)

    operator fun minus(other: Money): Money = Money(minorUnits - other.minorUnits)

    override fun compareTo(other: Money): Int = minorUnits.compareTo(other.minorUnits)

    /**
     * Applies a percentage expressed in basis points (1% = 100 bps, 15.5% = 1550 bps)
     * using exact half-up rounding.
     */
    fun percentageOfBasisPoints(basisPoints: Int): Money {
        require(basisPoints >= 0) { "Basis points must be non-negative" }
        if (minorUnits == 0L || basisPoints == 0) return ZERO
        val numerator = minorUnits * basisPoints.toLong()
        val rounded = (numerator + HALF_BASIS_DIVISOR) / BASIS_POINTS_DIVISOR
        return Money(rounded)
    }

    /**
     * Splits this amount evenly across [count] shares, distributing any indivisible remainder
     * (+1 minor unit) deterministically to the first `minorUnits % count` shares so that
     * the sum of all shares always equals [minorUnits].
     */
    fun splitEvenly(count: Int): List<Money> {
        require(count > 0) { "Participant count must be positive" }
        val base = minorUnits / count
        val remainder = (minorUnits % count).toInt()
        return List(count) { index ->
            val extra = if (index < remainder) 1L else 0L
            Money(base + extra)
        }
    }

    companion object {
        val ZERO = Money(0L)
        const val BASIS_POINTS_PER_PERCENT = 100
        private const val BASIS_POINTS_DIVISOR = 10_000L
        private const val HALF_BASIS_DIVISOR = 5_000L
    }
}
