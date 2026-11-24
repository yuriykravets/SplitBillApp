package com.partitionsoft.splitbillapp.presentation.calculator

import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.domain.model.BillCalculationResult
import com.partitionsoft.splitbillapp.domain.model.BillItem
import com.partitionsoft.splitbillapp.domain.model.Participant
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.domain.model.TipOption
import com.partitionsoft.splitbillapp.domain.model.ValidationError

data class BillCalculatorUiState(
    val splitMode: SplitMode = SplitMode.EQUAL,
    val billAmountText: String = "",
    val currency: Currency = Currency.USD,
    val selectedTipOption: TipOption = TipOption.Preset(15),
    val customTipText: String = "",
    val showAdditionalCharge: Boolean = false,
    val additionalChargeText: String = "",
    val note: String = "",
    val participants: List<Participant> = defaultParticipants(),
    val items: List<BillItem> = emptyList(),
    val showParticipantNames: Boolean = false,
    val editingParticipantId: String? = null,
    val showItemEditor: Boolean = false,
    val editingItemId: String? = null,
    val defaultItemParticipantId: String? = null,
    val showCurrencyPicker: Boolean = false,
    val calculationResult: BillCalculationResult? = null,
    val billAmountError: ValidationError? = null,
    val customTipError: ValidationError? = null,
    val additionalChargeError: ValidationError? = null,
    val activeSavedId: String? = null,
    val isSavedUpToDate: Boolean = false,
    val historyCount: Int = 0,
) {
    val participantCount: Int get() = participants.size

    val canShareOrSave: Boolean
        get() = calculationResult != null &&
            billAmountError == null &&
            customTipError == null &&
            additionalChargeError == null

    val isDirty: Boolean
        get() = billAmountText.isNotBlank() ||
            additionalChargeText.isNotBlank() ||
            note.isNotBlank() ||
            items.isNotEmpty() ||
            participants.any { it.name.isNotBlank() }

    companion object {
        fun defaultParticipants(count: Int = 2): List<Participant> =
            List(count) { index ->
                Participant(id = "p_${index + 1}", name = "")
            }
    }
}

sealed interface BillCalculatorEvent {
    data object SavedToHistory : BillCalculatorEvent
}
