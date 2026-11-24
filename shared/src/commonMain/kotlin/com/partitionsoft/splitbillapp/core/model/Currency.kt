package com.partitionsoft.splitbillapp.core.model

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val fractionDigits: Int = 2,
    val symbolAfterAmount: Boolean = false,
) {
    USD("USD", "$", "US Dollar", 2, false),
    EUR("EUR", "€", "Euro", 2, false),
    GBP("GBP", "£", "British Pound", 2, false),
    UAH("UAH", "₴", "Ukrainian Hryvnia", 2, true),
    PLN("PLN", "zł", "Polish Zloty", 2, true),
    CAD("CAD", "CA$", "Canadian Dollar", 2, false),
    AUD("AUD", "A$", "Australian Dollar", 2, false),
    CHF("CHF", "CHF", "Swiss Franc", 2, false),
    JPY("JPY", "¥", "Japanese Yen", 0, false);

    val minorUnitsPerMajor: Long
        get() {
            var factor = 1L
            repeat(fractionDigits) { factor *= 10L }
            return factor
        }

    companion object {
        fun fromCode(code: String): Currency =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
    }
}
