package com.partitionsoft.splitbillapp.di

import com.partitionsoft.splitbillapp.core.platform.DatabaseDriverFactory
import com.partitionsoft.splitbillapp.core.platform.IosDatabaseDriverFactory
import com.partitionsoft.splitbillapp.core.platform.IosKeyValueStorage
import com.partitionsoft.splitbillapp.core.platform.IosShareManager
import com.partitionsoft.splitbillapp.core.platform.IosTimeProvider
import com.partitionsoft.splitbillapp.core.platform.KeyValueStorage
import com.partitionsoft.splitbillapp.core.platform.ShareManager
import com.partitionsoft.splitbillapp.core.platform.TimeProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { IosDatabaseDriverFactory() }
    single<KeyValueStorage> { IosKeyValueStorage() }
    single<ShareManager> { IosShareManager() }
    single<TimeProvider> { IosTimeProvider() }
}
