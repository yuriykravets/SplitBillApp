package com.partitionsoft.splitbillapp.core.platform

import android.content.Context
import android.content.Intent
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.core.localization.stringsFor
import com.partitionsoft.splitbillapp.db.SplitBillDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AndroidPlatformContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun requireContext(): Context =
        requireNotNull(appContext) { "AndroidPlatformContext.init(context) must be called before using platform services." }
}

class AndroidDatabaseDriverFactory(
    private val context: Context,
) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver =
        AndroidSqliteDriver(SplitBillDatabase.Schema, context, "split_bill.db")
}

class AndroidKeyValueStorage(
    context: Context,
) : KeyValueStorage {
    private val prefs = context.getSharedPreferences("split_bill_settings", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}

class AndroidShareManager(
    private val context: Context,
) : ShareManager {
    override fun shareText(text: String, title: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}

class AndroidTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()

    override fun formatRelativeTimestamp(epochMillis: Long, language: AppLanguage): String {
        val strings = stringsFor(language)
        val locale = Locale.forLanguageTag(language.code)
        val diff = System.currentTimeMillis() - epochMillis
        return when {
            diff < 60_000L -> strings.justNow
            diff < 3_600_000L -> strings.minutesAgo(diff / 60_000L)
            diff < 86_400_000L -> SimpleDateFormat("HH:mm", locale).format(Date(epochMillis))
            else -> SimpleDateFormat("MMM d, HH:mm", locale).format(Date(epochMillis))
        }
    }
}
