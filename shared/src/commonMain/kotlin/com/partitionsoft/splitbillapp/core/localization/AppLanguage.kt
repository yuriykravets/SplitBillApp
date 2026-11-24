package com.partitionsoft.splitbillapp.core.localization

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
) {
    ENGLISH("en", "English", "English"),
    UKRAINIAN("uk", "Українська", "Ukrainian");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
    }
}
