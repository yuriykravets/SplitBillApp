package com.partitionsoft.splitbillapp.presentation.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partitionsoft.splitbillapp.core.localization.LocalAppStrings
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.presentation.calculator.components.AddEditItemDialog
import com.partitionsoft.splitbillapp.presentation.calculator.components.BillInputSection
import com.partitionsoft.splitbillapp.presentation.calculator.components.CurrencyPickerSheet
import com.partitionsoft.splitbillapp.presentation.calculator.components.EditParticipantNameDialog
import com.partitionsoft.splitbillapp.presentation.calculator.components.HeroResultCard
import com.partitionsoft.splitbillapp.presentation.calculator.components.ItemsSection
import com.partitionsoft.splitbillapp.presentation.calculator.components.ParticipantsSection
import com.partitionsoft.splitbillapp.presentation.calculator.components.SplitModeSelector
import com.partitionsoft.splitbillapp.presentation.calculator.components.TipSelectorSection
import com.partitionsoft.splitbillapp.presentation.icons.AppIcons

@Composable
fun BillCalculatorScreen(
    viewModel: BillCalculatorViewModel,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val strings = LocalAppStrings.current

    LaunchedEffect(viewModel, strings) {
        viewModel.events.collect { event ->
            when (event) {
                BillCalculatorEvent.SavedToHistory -> {
                    snackbarHostState.showSnackbar(strings.savedToHistoryMessage)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                },
                actions = {
                    Surface(
                        onClick = { viewModel.onCurrencyPickerVisibilityChanged(true) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${uiState.currency.symbol} ${uiState.currency.code}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = AppIcons.ChevronDown,
                                contentDescription = strings.changeCurrencyCd,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (uiState.isDirty) {
                        IconButton(onClick = viewModel::onReset) {
                            Icon(
                                imageVector = AppIcons.Refresh,
                                contentDescription = strings.resetBillCd,
                                modifier = Modifier.size(21.dp),
                            )
                        }
                    }

                    IconButton(onClick = onOpenHistory) {
                        BadgedBox(
                            badge = {
                                if (uiState.historyCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary,
                                    ) {
                                        Text(
                                            text = if (uiState.historyCount > 99) "99+" else uiState.historyCount.toString(),
                                        )
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = AppIcons.History,
                                contentDescription = strings.historyCd,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = strings.settingsCd,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            val isWideScreen = maxWidth >= 760.dp

            if (isWideScreen) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 1040.dp)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        HeroResultCard(
                            result = uiState.calculationResult,
                            splitMode = uiState.splitMode,
                            currency = uiState.currency,
                            participantCount = uiState.participantCount,
                            canShareOrSave = uiState.canShareOrSave,
                            isSavedUpToDate = uiState.isSavedUpToDate,
                            onShareClick = viewModel::onShareResult,
                            onSaveClick = viewModel::onSaveToHistory,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        CalculatorControlsColumn(uiState = uiState, viewModel = viewModel)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    HeroResultCard(
                        result = uiState.calculationResult,
                        splitMode = uiState.splitMode,
                        currency = uiState.currency,
                        participantCount = uiState.participantCount,
                        canShareOrSave = uiState.canShareOrSave,
                        isSavedUpToDate = uiState.isSavedUpToDate,
                        onShareClick = viewModel::onShareResult,
                        onSaveClick = viewModel::onSaveToHistory,
                    )

                    CalculatorControlsColumn(uiState = uiState, viewModel = viewModel)

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (uiState.showCurrencyPicker) {
        CurrencyPickerSheet(
            selectedCurrency = uiState.currency,
            onCurrencySelected = viewModel::onCurrencySelected,
            onDismiss = { viewModel.onCurrencyPickerVisibilityChanged(false) },
        )
    }

    val editingParticipantIndex = uiState.editingParticipantId?.let { id ->
        uiState.participants.indexOfFirst { it.id == id }.takeIf { it >= 0 }
    }
    if (editingParticipantIndex != null) {
        val participant = uiState.participants[editingParticipantIndex]
        EditParticipantNameDialog(
            participant = participant,
            index = editingParticipantIndex,
            onDismiss = viewModel::onDismissParticipantEditor,
            onConfirm = { newName ->
                viewModel.onUpdateParticipantName(participant.id, newName)
            },
        )
    }

    if (uiState.showItemEditor) {
        val existingItem = uiState.editingItemId?.let { id ->
            uiState.items.firstOrNull { it.id == id }
        }
        AddEditItemDialog(
            existingItem = existingItem,
            defaultParticipantId = uiState.defaultItemParticipantId,
            participants = uiState.participants,
            currency = uiState.currency,
            sanitizePrice = viewModel::sanitizePriceForCurrentCurrency,
            parsePrice = viewModel::parseItemPrice,
            onDismiss = viewModel::onDismissItemEditor,
            onConfirm = viewModel::onSaveItem,
        )
    }
}

@Composable
private fun CalculatorControlsColumn(
    uiState: BillCalculatorUiState,
    viewModel: BillCalculatorViewModel,
) {
    SplitModeSelector(
        selectedMode = uiState.splitMode,
        onModeSelected = viewModel::onSplitModeChanged,
    )

    if (uiState.splitMode == SplitMode.BY_ITEMS) {
        ParticipantsSection(
            participants = uiState.participants,
            result = uiState.calculationResult,
            splitMode = uiState.splitMode,
            currency = uiState.currency,
            showParticipantNames = uiState.showParticipantNames,
            onIncrementPeople = viewModel::onIncrementPeople,
            onDecrementPeople = viewModel::onDecrementPeople,
            onToggleParticipantNames = viewModel::onToggleParticipantNames,
            onEditParticipantClick = { participant ->
                viewModel.onStartEditingParticipant(participant.id)
            },
            onQuickAddItemForParticipant = { participant ->
                viewModel.onStartAddingItem(defaultParticipantId = participant.id)
            },
        )

        ItemsSection(
            items = uiState.items,
            participants = uiState.participants,
            currency = uiState.currency,
            showAdditionalCharge = uiState.showAdditionalCharge,
            additionalChargeText = uiState.additionalChargeText,
            additionalChargeError = uiState.additionalChargeError,
            note = uiState.note,
            onAddItemClick = { viewModel.onStartAddingItem() },
            onEditItemClick = { item -> viewModel.onStartEditingItem(item.id) },
            onDeleteItemClick = { item -> viewModel.onDeleteItem(item.id) },
            onToggleAdditionalCharge = viewModel::onToggleAdditionalCharge,
            onAdditionalChargeChanged = viewModel::onAdditionalChargeChanged,
            onNoteChanged = viewModel::onNoteChanged,
        )

        TipSelectorSection(
            selectedTipOption = uiState.selectedTipOption,
            customTipText = uiState.customTipText,
            customTipError = uiState.customTipError,
            currency = uiState.currency,
            onTipOptionSelected = viewModel::onTipOptionSelected,
            onCustomTipChanged = viewModel::onCustomTipChanged,
        )
    } else {
        BillInputSection(
            billAmountText = uiState.billAmountText,
            billAmountError = uiState.billAmountError,
            currency = uiState.currency,
            showAdditionalCharge = uiState.showAdditionalCharge,
            additionalChargeText = uiState.additionalChargeText,
            additionalChargeError = uiState.additionalChargeError,
            note = uiState.note,
            onBillAmountChanged = viewModel::onBillAmountChanged,
            onToggleAdditionalCharge = viewModel::onToggleAdditionalCharge,
            onAdditionalChargeChanged = viewModel::onAdditionalChargeChanged,
            onNoteChanged = viewModel::onNoteChanged,
        )

        TipSelectorSection(
            selectedTipOption = uiState.selectedTipOption,
            customTipText = uiState.customTipText,
            customTipError = uiState.customTipError,
            currency = uiState.currency,
            onTipOptionSelected = viewModel::onTipOptionSelected,
            onCustomTipChanged = viewModel::onCustomTipChanged,
        )

        ParticipantsSection(
            participants = uiState.participants,
            result = uiState.calculationResult,
            splitMode = uiState.splitMode,
            currency = uiState.currency,
            showParticipantNames = uiState.showParticipantNames,
            onIncrementPeople = viewModel::onIncrementPeople,
            onDecrementPeople = viewModel::onDecrementPeople,
            onToggleParticipantNames = viewModel::onToggleParticipantNames,
            onEditParticipantClick = { participant ->
                viewModel.onStartEditingParticipant(participant.id)
            },
        )
    }
}
