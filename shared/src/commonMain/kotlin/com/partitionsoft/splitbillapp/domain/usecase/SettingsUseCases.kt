package com.partitionsoft.splitbillapp.domain.usecase

import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveAppLanguageUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppLanguage> = repository.observeLanguage()
}

class SetAppLanguageUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
