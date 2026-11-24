package com.partitionsoft.splitbillapp.presentation.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.splitbillapp.core.format.MoneyFormatter
import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money
import com.partitionsoft.splitbillapp.core.platform.ShareManager
import com.partitionsoft.splitbillapp.domain.model.BillInput
import com.partitionsoft.splitbillapp.domain.model.BillItem
import com.partitionsoft.splitbillapp.domain.model.Participant
import com.partitionsoft.splitbillapp.domain.model.SavedBillSplit
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.domain.model.TipOption
import com.partitionsoft.splitbillapp.domain.model.ValidationError
import com.partitionsoft.splitbillapp.domain.repository.SettingsRepository
import com.partitionsoft.splitbillapp.domain.usecase.CalculateBillSplitUseCase
import com.partitionsoft.splitbillapp.domain.usecase.CalculationOutcome
import com.partitionsoft.splitbillapp.domain.usecase.FormatShareSummaryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ObserveBillHistoryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ParseInputUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ParseMoneyResult
import com.partitionsoft.splitbillapp.domain.usecase.ParseTipResult
import com.partitionsoft.splitbillapp.domain.usecase.SaveBillSplitUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BillCalculatorViewModel(
    private val parseInput: ParseInputUseCase,
    private val calculateBillSplit: CalculateBillSplitUseCase,
    private val formatShareSummary: FormatShareSummaryUseCase,
    private val saveBillSplit: SaveBillSplitUseCase,
    private val observeBillHistory: ObserveBillHistoryUseCase,
    private val settingsRepository: SettingsRepository,
    private val shareManager: ShareManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillCalculatorUiState())
    val uiState: StateFlow<BillCalculatorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BillCalculatorEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<BillCalculatorEvent> = _events.asSharedFlow()

    private var nextParticipantSeq = 3
    private var nextItemSeq = 1

    init {
        viewModelScope.launch {
            observeBillHistory().collect { history ->
                _uiState.update { it.copy(historyCount = history.size) }
            }
        }
    }

    fun onSplitModeChanged(mode: SplitMode) {
        if (_uiState.value.splitMode == mode) return
        updateAndRecalculate {
            copy(
                splitMode = mode,
                showParticipantNames = if (mode == SplitMode.BY_ITEMS) true else showParticipantNames,
                billAmountError = null,
                isSavedUpToDate = false,
            )
        }
    }

    fun onStartAddingItem(defaultParticipantId: String? = null) {
        _uiState.update {
            it.copy(
                showItemEditor = true,
                editingItemId = null,
                defaultItemParticipantId = defaultParticipantId ?: it.participants.firstOrNull()?.id,
            )
        }
    }

    fun onStartEditingItem(itemId: String) {
        _uiState.update {
            it.copy(
                showItemEditor = true,
                editingItemId = itemId,
                defaultItemParticipantId = null,
            )
        }
    }

    fun onDismissItemEditor() {
        _uiState.update {
            it.copy(
                showItemEditor = false,
                editingItemId = null,
                defaultItemParticipantId = null,
            )
        }
    }

    fun onSaveItem(name: String, priceText: String, assignedParticipantIds: Set<String>) {
        val state = _uiState.value
        val parsed = parseInput.parseBillAmount(priceText, state.currency)
        if (parsed !is ParseMoneyResult.Valid) return
        val validIds = state.participants.map { it.id }.toSet()
        val cleanedIds = assignedParticipantIds.intersect(validIds)
        if (cleanedIds.isEmpty()) return

        val trimmedName = name.trim().take(40)
        val editingId = state.editingItemId

        updateAndRecalculate {
            val updatedItems = if (editingId != null) {
                items.map { item ->
                    if (item.id == editingId) {
                        item.copy(
                            name = trimmedName,
                            price = parsed.money,
                            assignedParticipantIds = cleanedIds,
                        )
                    } else {
                        item
                    }
                }
            } else {
                items + BillItem(
                    id = "item_${nextItemSeq++}",
                    name = trimmedName,
                    price = parsed.money,
                    assignedParticipantIds = cleanedIds,
                )
            }
            copy(
                items = updatedItems,
                showItemEditor = false,
                editingItemId = null,
                defaultItemParticipantId = null,
                isSavedUpToDate = false,
            )
        }
    }

    fun onDeleteItem(itemId: String) {
        updateAndRecalculate {
            copy(
                items = items.filterNot { it.id == itemId },
                isSavedUpToDate = false,
            )
        }
    }

    fun parseItemPrice(raw: String): ParseMoneyResult =
        parseInput.parseBillAmount(raw, _uiState.value.currency)

    fun sanitizePriceForCurrentCurrency(raw: String): String =
        sanitizeDecimalInput(raw, _uiState.value.currency.fractionDigits)

    fun onBillAmountChanged(raw: String) {
        val sanitized = sanitizeDecimalInput(raw, _uiState.value.currency.fractionDigits)
        updateAndRecalculate {
            copy(billAmountText = sanitized, isSavedUpToDate = false)
        }
    }

    fun onCurrencyPickerVisibilityChanged(visible: Boolean) {
        _uiState.update { it.copy(showCurrencyPicker = visible) }
    }

    fun onCurrencySelected(currency: Currency) {
        updateAndRecalculate {
            val adjustedBill = if (currency.fractionDigits == 0) {
                billAmountText.substringBefore('.').substringBefore(',')
            } else {
                billAmountText
            }
            val adjustedExtra = if (currency.fractionDigits == 0) {
                additionalChargeText.substringBefore('.').substringBefore(',')
            } else {
                additionalChargeText
            }
            copy(
                currency = currency,
                billAmountText = adjustedBill,
                additionalChargeText = adjustedExtra,
                showCurrencyPicker = false,
                isSavedUpToDate = false,
            )
        }
    }

    fun onTipOptionSelected(option: TipOption) {
        updateAndRecalculate {
            copy(
                selectedTipOption = option,
                customTipError = if (option is TipOption.Preset) null else customTipError,
                isSavedUpToDate = false,
            )
        }
    }

    fun onCustomTipChanged(raw: String) {
        val sanitized = sanitizeDecimalInput(raw, maxFractionDigits = 2)
        updateAndRecalculate {
            copy(
                selectedTipOption = TipOption.Custom,
                customTipText = sanitized,
                isSavedUpToDate = false,
            )
        }
    }

    fun onToggleAdditionalCharge() {
        updateAndRecalculate {
            val nextShow = !showAdditionalCharge
            copy(
                showAdditionalCharge = nextShow,
                additionalChargeText = if (nextShow) additionalChargeText else "",
                additionalChargeError = if (nextShow) additionalChargeError else null,
                isSavedUpToDate = false,
            )
        }
    }

    fun onAdditionalChargeChanged(raw: String) {
        val sanitized = sanitizeDecimalInput(raw, _uiState.value.currency.fractionDigits)
        updateAndRecalculate {
            copy(additionalChargeText = sanitized, isSavedUpToDate = false)
        }
    }

    fun onNoteChanged(note: String) {
        _uiState.update {
            it.copy(note = note.take(60), isSavedUpToDate = false)
        }
    }

    fun onIncrementPeople() {
        val current = _uiState.value.participants
        if (current.size >= CalculateBillSplitUseCase.MAX_PARTICIPANTS) return
        val newParticipant = Participant(id = "p_${nextParticipantSeq++}", name = "")
        updateAndRecalculate {
            copy(
                participants = participants + newParticipant,
                isSavedUpToDate = false,
            )
        }
    }

    fun onDecrementPeople() {
        val current = _uiState.value.participants
        if (current.size <= 1) return
        val removed = current.last()
        val remaining = current.dropLast(1)
        val fallbackId = remaining.first().id
        updateAndRecalculate {
            val updatedItems = items.map { item ->
                if (removed.id in item.assignedParticipantIds) {
                    val nextIds = item.assignedParticipantIds - removed.id
                    item.copy(assignedParticipantIds = nextIds.ifEmpty { setOf(fallbackId) })
                } else {
                    item
                }
            }
            copy(
                participants = remaining,
                items = updatedItems,
                editingParticipantId = if (editingParticipantId == removed.id) null else editingParticipantId,
                isSavedUpToDate = false,
            )
        }
    }

    fun onToggleParticipantNames() {
        _uiState.update { it.copy(showParticipantNames = !it.showParticipantNames) }
    }

    fun onStartEditingParticipant(participantId: String) {
        _uiState.update { it.copy(editingParticipantId = participantId) }
    }

    fun onDismissParticipantEditor() {
        _uiState.update { it.copy(editingParticipantId = null) }
    }

    fun onUpdateParticipantName(participantId: String, newName: String) {
        val trimmed = newName.take(32)
        updateAndRecalculate {
            copy(
                participants = participants.map { p ->
                    if (p.id == participantId) p.copy(name = trimmed) else p
                },
                editingParticipantId = null,
                isSavedUpToDate = false,
            )
        }
    }

    fun onSaveToHistory() {
        val state = _uiState.value
        val result = state.calculationResult ?: return
        val input = buildCurrentInput(state) ?: return
        val language = settingsRepository.currentLanguage()
        viewModelScope.launch {
            val saved = saveBillSplit(
                input = input,
                result = result,
                existingId = state.activeSavedId,
                language = language,
            )
            _uiState.update {
                it.copy(
                    activeSavedId = saved.id,
                    isSavedUpToDate = true,
                )
            }
            _events.emit(BillCalculatorEvent.SavedToHistory)
        }
    }

    fun onShareResult() {
        val state = _uiState.value
        val result = state.calculationResult ?: return
        val language = settingsRepository.currentLanguage()
        val summary = formatShareSummary(result = result, note = state.note, language = language)
        shareManager.shareText(text = summary, title = "Split Bill")
    }

    fun onReopenSavedSplit(saved: SavedBillSplit) {
        val tipBps = saved.input.tipBasisPoints
        val matchingPreset = TipOption.presets.firstOrNull { it.percent * 100 == tipBps }
        val tipOption: TipOption = matchingPreset ?: TipOption.Custom
        val customTip = if (matchingPreset == null) {
            MoneyFormatter.formatEditablePercentage(tipBps)
        } else {
            ""
        }
        nextParticipantSeq = maxOf(nextParticipantSeq, saved.input.participants.size + 1)
        nextItemSeq = maxOf(nextItemSeq, saved.input.items.size + 1)

        updateAndRecalculate {
            copy(
                splitMode = saved.input.splitMode,
                billAmountText = if (saved.input.splitMode == SplitMode.EQUAL) {
                    MoneyFormatter.formatEditable(saved.input.billAmount, saved.input.currency)
                } else {
                    ""
                },
                currency = saved.input.currency,
                selectedTipOption = tipOption,
                customTipText = customTip,
                showAdditionalCharge = saved.input.additionalCharge.minorUnits > 0L,
                additionalChargeText = MoneyFormatter.formatEditable(saved.input.additionalCharge, saved.input.currency),
                note = saved.input.note,
                participants = saved.input.participants,
                items = saved.input.items,
                showParticipantNames = saved.input.splitMode == SplitMode.BY_ITEMS ||
                    saved.input.participants.any { it.name.isNotBlank() },
                editingParticipantId = null,
                showItemEditor = false,
                editingItemId = null,
                defaultItemParticipantId = null,
                activeSavedId = saved.id,
                isSavedUpToDate = true,
            )
        }
    }

    fun onReset() {
        val keepCurrency = _uiState.value.currency
        val keepHistoryCount = _uiState.value.historyCount
        val keepMode = _uiState.value.splitMode
        nextParticipantSeq = 3
        nextItemSeq = 1
        _uiState.value = BillCalculatorUiState(
            splitMode = keepMode,
            currency = keepCurrency,
            historyCount = keepHistoryCount,
            showParticipantNames = keepMode == SplitMode.BY_ITEMS,
        )
    }

    private fun updateAndRecalculate(transform: BillCalculatorUiState.() -> BillCalculatorUiState) {
        _uiState.update { current ->
            val updated = current.transform()
            recalculate(updated)
        }
    }

    private fun recalculate(state: BillCalculatorUiState): BillCalculatorUiState {
        // 1. Parse tip
        val tipParsed = when (val opt = state.selectedTipOption) {
            is TipOption.Preset -> ParseTipResult.Valid(opt.percent * 100)
            is TipOption.Custom -> parseInput.parseTipPercentage(state.customTipText)
        }
        val tipError = (tipParsed as? ParseTipResult.Invalid)?.error

        // 2. Parse additional charge
        val extraParsed = if (state.showAdditionalCharge) {
            parseInput.parseAdditionalCharge(state.additionalChargeText, state.currency)
        } else {
            ParseMoneyResult.Valid(Money.ZERO)
        }
        val extraError = (extraParsed as? ParseMoneyResult.Invalid)?.error

        if (state.splitMode == SplitMode.BY_ITEMS) {
            if (state.items.isEmpty() ||
                tipParsed !is ParseTipResult.Valid ||
                extraParsed !is ParseMoneyResult.Valid
            ) {
                return state.copy(
                    calculationResult = null,
                    billAmountError = null,
                    customTipError = tipError,
                    additionalChargeError = extraError,
                )
            }

            val itemsTotal = Money(state.items.sumOf { it.price.minorUnits })
            val input = BillInput(
                billAmount = itemsTotal,
                currency = state.currency,
                tipBasisPoints = tipParsed.basisPoints,
                additionalCharge = extraParsed.money,
                participants = state.participants,
                note = state.note,
                splitMode = SplitMode.BY_ITEMS,
                items = state.items,
            )

            return when (val outcome = calculateBillSplit(input)) {
                is CalculationOutcome.Success -> state.copy(
                    calculationResult = outcome.result,
                    billAmountError = null,
                    customTipError = null,
                    additionalChargeError = null,
                )
                is CalculationOutcome.Failure -> state.copy(
                    calculationResult = null,
                    billAmountError = outcome.error,
                    customTipError = tipError,
                    additionalChargeError = extraError,
                )
            }
        }

        // 3. Parse bill amount (EQUAL mode)
        val billParsed = parseInput.parseBillAmount(state.billAmountText, state.currency)
        val billError = when (billParsed) {
            is ParseMoneyResult.Valid -> null
            is ParseMoneyResult.Invalid -> {
                if (billParsed.error == ValidationError.EmptyBillAmount) null
                else billParsed.error
            }
        }

        if (billParsed !is ParseMoneyResult.Valid ||
            tipParsed !is ParseTipResult.Valid ||
            extraParsed !is ParseMoneyResult.Valid
        ) {
            return state.copy(
                calculationResult = null,
                billAmountError = billError,
                customTipError = tipError,
                additionalChargeError = extraError,
            )
        }

        val input = BillInput(
            billAmount = billParsed.money,
            currency = state.currency,
            tipBasisPoints = tipParsed.basisPoints,
            additionalCharge = extraParsed.money,
            participants = state.participants,
            note = state.note,
            splitMode = SplitMode.EQUAL,
            items = emptyList(),
        )

        return when (val outcome = calculateBillSplit(input)) {
            is CalculationOutcome.Success -> state.copy(
                calculationResult = outcome.result,
                billAmountError = null,
                customTipError = null,
                additionalChargeError = null,
            )
            is CalculationOutcome.Failure -> state.copy(
                calculationResult = null,
                billAmountError = outcome.error,
                customTipError = tipError,
                additionalChargeError = extraError,
            )
        }
    }

    private fun buildCurrentInput(state: BillCalculatorUiState): BillInput? {
        val result = state.calculationResult ?: return null
        return BillInput(
            billAmount = result.billAmount,
            currency = result.currency,
            tipBasisPoints = result.tipBasisPoints,
            additionalCharge = result.additionalCharge,
            participants = state.participants,
            note = state.note,
            splitMode = state.splitMode,
            items = if (state.splitMode == SplitMode.BY_ITEMS) state.items else emptyList(),
        )
    }

    private fun sanitizeDecimalInput(raw: String, maxFractionDigits: Int): String {
        val filtered = buildString {
            var separatorSeen = false
            var fractionCount = 0
            for (ch in raw) {
                when {
                    ch.isDigit() -> {
                        if (!separatorSeen) {
                            append(ch)
                        } else if (fractionCount < maxFractionDigits) {
                            append(ch)
                            fractionCount++
                        }
                    }
                    (ch == '.' || ch == ',') && maxFractionDigits > 0 && !separatorSeen -> {
                        separatorSeen = true
                        append('.')
                    }
                }
            }
        }
        return filtered.take(12)
    }
}
