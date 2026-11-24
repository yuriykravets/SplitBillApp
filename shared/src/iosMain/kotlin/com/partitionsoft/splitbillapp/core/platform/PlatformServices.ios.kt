package com.partitionsoft.splitbillapp.core.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.core.localization.stringsFor
import com.partitionsoft.splitbillapp.db.SplitBillDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver =
        NativeSqliteDriver(SplitBillDatabase.Schema, "split_bill.db")
}

class IosKeyValueStorage : KeyValueStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
}

class IosShareManager : ShareManager {
    @OptIn(ExperimentalForeignApi::class)
    override fun shareText(text: String, title: String) {
        val activityController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        val rootController = findTopViewController() ?: return
        activityController.popoverPresentationController?.let { popover ->
            popover.sourceView = rootController.view
            popover.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)
        }
        rootController.presentViewController(activityController, animated = true, completion = null)
    }

    private fun findTopViewController(): platform.UIKit.UIViewController? {
        val windowScene = UIApplication.sharedApplication.connectedScenes
            .firstOrNull { it is UIWindowScene } as? UIWindowScene
        val keyWindow = windowScene?.windows
            ?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
            ?: windowScene?.windows?.firstOrNull() as? UIWindow
        var top = keyWindow?.rootViewController
        while (top?.presentedViewController != null) {
            top = top.presentedViewController
        }
        return top
    }
}

class IosTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long =
        (NSDate().timeIntervalSince1970 * 1000.0).toLong()

    override fun formatRelativeTimestamp(epochMillis: Long, language: AppLanguage): String {
        val strings = stringsFor(language)
        val now = currentTimeMillis()
        val diff = now - epochMillis
        return when {
            diff < 60_000L -> strings.justNow
            diff < 3_600_000L -> strings.minutesAgo(diff / 60_000L)
            else -> {
                val formatter = NSDateFormatter().apply {
                    locale = NSLocale(localeIdentifier = language.code)
                    dateFormat = if (diff < 86_400_000L) "HH:mm" else "MMM d, HH:mm"
                }
                val date = NSDate.dateWithTimeIntervalSince1970(epochMillis.toDouble() / 1000.0)
                formatter.stringFromDate(date)
            }
        }
    }
}
