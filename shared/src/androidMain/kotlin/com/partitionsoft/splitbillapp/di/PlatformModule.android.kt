package com.partitionsoft.splitbillapp.di

import com.partitionsoft.splitbillapp.core.platform.AndroidDatabaseDriverFactory
import com.partitionsoft.splitbillapp.core.platform.AndroidKeyValueStorage
import com.partitionsoft.splitbillapp.core.platform.AndroidPlatformContext
import com.partitionsoft.splitbillapp.core.platform.AndroidShareManager
import com.partitionsoft.splitbillapp.core.platform.AndroidTimeProvider
import com.partitionsoft.splitbillapp.core.platform.DatabaseDriverFactory
import com.partitionsoft.splitbillapp.core.platform.KeyValueStorage
import com.partitionsoft.splitbillapp.core.platform.ShareManager
import com.partitionsoft.splitbillapp.core.platform.TimeProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(AndroidPlatformContext.requireContext()) }
    single<KeyValueStorage> { AndroidKeyValueStorage(AndroidPlatformContext.requireContext()) }
    single<ShareManager> { AndroidShareManager(AndroidPlatformContext.requireContext()) }
    single<TimeProvider> { AndroidTimeProvider() }
}
