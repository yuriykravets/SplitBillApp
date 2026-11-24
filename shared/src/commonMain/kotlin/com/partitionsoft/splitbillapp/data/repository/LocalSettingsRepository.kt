package com.partitionsoft.splitbillapp.data.repository

import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.core.platform.KeyValueStorage
import com.partitionsoft.splitbillapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalSettingsRepository(
    private val storage: KeyValueStorage,
) : SettingsRepository {

    private val languageFlow = MutableStateFlow(
        AppLanguage.fromCode(storage.getString(KEY_LANGUAGE)),
    )

    override fun observeLanguage(): Flow<AppLanguage> = languageFlow.asStateFlow()

    override fun currentLanguage(): AppLanguage = languageFlow.value

    override suspend fun setLanguage(language: AppLanguage) {
        storage.putString(KEY_LANGUAGE, language.code)
        languageFlow.value = language
    }

    companion object {
        private const val KEY_LANGUAGE = "app_language"
    }
}
