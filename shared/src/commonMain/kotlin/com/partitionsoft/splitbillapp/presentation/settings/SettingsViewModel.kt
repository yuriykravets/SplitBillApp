package com.partitionsoft.splitbillapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.domain.usecase.ObserveAppLanguageUseCase
import com.partitionsoft.splitbillapp.domain.usecase.SetAppLanguageUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeAppLanguage: ObserveAppLanguageUseCase,
    private val setAppLanguage: SetAppLanguageUseCase,
) : ViewModel() {

    val language: StateFlow<AppLanguage> = observeAppLanguage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppLanguage.ENGLISH,
        )

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            setAppLanguage(language)
        }
    }
}
