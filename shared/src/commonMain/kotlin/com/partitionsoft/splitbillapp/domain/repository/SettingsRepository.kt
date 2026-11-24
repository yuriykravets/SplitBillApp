package com.partitionsoft.splitbillapp.domain.repository

import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeLanguage(): Flow<AppLanguage>
    fun currentLanguage(): AppLanguage
    suspend fun setLanguage(language: AppLanguage)
}
