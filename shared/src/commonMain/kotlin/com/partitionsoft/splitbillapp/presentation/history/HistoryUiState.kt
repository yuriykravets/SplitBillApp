package com.partitionsoft.splitbillapp.presentation.history

import com.partitionsoft.splitbillapp.domain.model.SavedBillSplit

data class HistoryUiState(
    val items: List<SavedBillSplit> = emptyList(),
    val isLoading: Boolean = true,
    val showClearConfirmation: Boolean = false,
)
