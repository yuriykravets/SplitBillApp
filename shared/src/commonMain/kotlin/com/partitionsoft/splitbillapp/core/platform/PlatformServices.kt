package com.partitionsoft.splitbillapp.core.platform

import app.cash.sqldelight.db.SqlDriver
import com.partitionsoft.splitbillapp.core.localization.AppLanguage

interface ShareManager {
    fun shareText(text: String, title: String = "Split Bill")
}

interface TimeProvider {
    fun currentTimeMillis(): Long
    fun formatRelativeTimestamp(epochMillis: Long, language: AppLanguage = AppLanguage.ENGLISH): String
}

interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

interface KeyValueStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}
