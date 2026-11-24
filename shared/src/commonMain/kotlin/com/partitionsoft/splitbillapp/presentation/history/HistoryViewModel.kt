package com.partitionsoft.splitbillapp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.core.platform.TimeProvider
import com.partitionsoft.splitbillapp.domain.usecase.ClearBillHistoryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.DeleteBillSplitUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ObserveBillHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val observeBillHistory: ObserveBillHistoryUseCase,
    private val deleteBillSplit: DeleteBillSplitUseCase,
    private val clearBillHistory: ClearBillHistoryUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeBillHistory().collect { splits ->
                _uiState.update {
                    it.copy(
                        items = splits,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun formatTimestamp(epochMillis: Long, language: AppLanguage): String =
        timeProvider.formatRelativeTimestamp(epochMillis, language)

    fun onDeleteItem(id: String) {
        viewModelScope.launch {
            deleteBillSplit(id)
        }
    }

    fun onRequestClearAll() {
        _uiState.update { it.copy(showClearConfirmation = true) }
    }

    fun onDismissClearAll() {
        _uiState.update { it.copy(showClearConfirmation = false) }
    }

    fun onConfirmClearAll() {
        _uiState.update { it.copy(showClearConfirmation = false) }
        viewModelScope.launch {
            clearBillHistory()
        }
    }
}
