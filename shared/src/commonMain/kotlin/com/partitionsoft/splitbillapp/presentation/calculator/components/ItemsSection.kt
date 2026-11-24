package com.partitionsoft.splitbillapp.presentation.calculator.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.partitionsoft.splitbillapp.core.format.MoneyFormatter
import com.partitionsoft.splitbillapp.core.localization.LocalAppStrings
import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money
import com.partitionsoft.splitbillapp.domain.model.BillItem
import com.partitionsoft.splitbillapp.domain.model.Participant
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.domain.model.ValidationError
import com.partitionsoft.splitbillapp.domain.usecase.ParseMoneyResult
import com.partitionsoft.splitbillapp.presentation.icons.AppIcons

@Composable
fun SplitModeSelector(
    selectedMode: SplitMode,
    onModeSelected: (SplitMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SplitModePill(
                label = strings.splitModeEqual,
                selected = selectedMode == SplitMode.EQUAL,
                onClick = { onModeSelected(SplitMode.EQUAL) },
                modifier = Modifier.weight(1f),
            )
            SplitModePill(
                label = strings.splitModeByItems,
                selected = selectedMode == SplitMode.BY_ITEMS,
                onClick = { onModeSelected(SplitMode.BY_ITEMS) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SplitModePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "modeContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "modeContent",
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ItemsSection(
    items: List<BillItem>,
    participants: List<Participant>,
    currency: Currency,
    showAdditionalCharge: Boolean,
    additionalChargeText: String,
    additionalChargeError: ValidationError?,
    note: String,
    onAddItemClick: () -> Unit,
    onEditItemClick: (BillItem) -> Unit,
    onDeleteItemClick: (BillItem) -> Unit,
    onToggleAdditionalCharge: () -> Unit,
    onAdditionalChargeChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = strings.ordersTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (items.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = strings.itemsCountLabel(items.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }

                FilledTonalButton(
                    onClick = onAddItemClick,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.addItem,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (items.isEmpty()) {
                Surface(
                    onClick = onAddItemClick,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = strings.noItemsYetTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.noItemsYetSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { item ->
                        BillItemRow(
                            item = item,
                            participants = participants,
                            currency = currency,
                            onClick = { onEditItemClick(item) },
                            onDeleteClick = { onDeleteItemClick(item) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = showAdditionalCharge,
                    onClick = onToggleAdditionalCharge,
                    shape = CircleShape,
                    label = {
                        Text(
                            text = if (showAdditionalCharge) strings.taxOrFeeAdded else strings.addTaxOrFee,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }

            AnimatedVisibility(
                visible = showAdditionalCharge,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = additionalChargeText,
                        onValueChange = onAdditionalChargeChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(strings.taxOrServiceChargeLabel) },
                        prefix = {
                            Text(
                                text = "${currency.symbol} ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        isError = additionalChargeError != null,
                        supportingText = additionalChargeError?.let { error ->
                            { Text(text = strings.validationMessage(error, currency)) }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (currency.fractionDigits == 0) KeyboardType.Number else KeyboardType.Decimal,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = strings.notePlaceholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                ),
            )
        }
    }
}

@Composable
private fun BillItemRow(
    item: BillItem,
    participants: List<Participant>,
    currency: Currency,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val assignedNames = participants.mapIndexedNotNull { index, participant ->
        if (participant.id in item.assignedParticipantIds) {
            participant.name.trim().ifEmpty { strings.defaultParticipantName(index) }
        } else {
            null
        }
    }
    val assigneeSummary = when {
        participants.size > 1 && assignedNames.size == participants.size -> strings.sharedByAll
        else -> assignedNames.joinToString(", ")
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (assigneeSummary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = assigneeSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = MoneyFormatter.format(item.price, currency),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum",
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = AppIcons.Delete,
                        contentDescription = strings.deleteItemCd,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditItemDialog(
    existingItem: BillItem?,
    defaultParticipantId: String?,
    participants: List<Participant>,
    currency: Currency,
    sanitizePrice: (String) -> String,
    parsePrice: (String) -> ParseMoneyResult,
    onDismiss: () -> Unit,
    onConfirm: (name: String, priceText: String, assignedParticipantIds: Set<String>) -> Unit,
) {
    val strings = LocalAppStrings.current
    var nameText by remember(existingItem?.id) { mutableStateOf(existingItem?.name ?: "") }
    var priceText by remember(existingItem?.id) {
        mutableStateOf(
            existingItem?.let { MoneyFormatter.formatEditable(it.price, currency) } ?: ""
        )
    }
    var selectedIds by remember(existingItem?.id, defaultParticipantId) {
        val initial = existingItem?.assignedParticipantIds
            ?: defaultParticipantId?.let { setOf(it) }
            ?: participants.firstOrNull()?.id?.let { setOf(it) }
            ?: emptySet()
        mutableStateOf(initial)
    }

    val priceParsed = parsePrice(priceText)
    val priceError = (priceParsed as? ParseMoneyResult.Invalid)?.error?.takeIf {
        it != ValidationError.EmptyBillAmount
    }
    val validMoney = (priceParsed as? ParseMoneyResult.Valid)?.money
    val allParticipantIds = participants.map { it.id }.toSet()
    val allSelected = allParticipantIds.isNotEmpty() && selectedIds == allParticipantIds
    val canSave = nameText.trim().isNotEmpty() && validMoney != null && selectedIds.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingItem != null) strings.editItemTitle else strings.addItemTitle,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it.take(40) },
                    singleLine = true,
                    label = { Text(strings.itemNameLabel) },
                    placeholder = { Text(strings.itemNamePlaceholder) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    strings.quickItemPresets.forEach { preset ->
                        SuggestionChip(
                            onClick = { nameText = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelMedium) },
                            shape = CircleShape,
                        )
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = sanitizePrice(it) },
                    singleLine = true,
                    label = { Text(strings.itemPriceLabel) },
                    prefix = {
                        Text(
                            text = "${currency.symbol} ",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    placeholder = {
                        Text(if (currency.fractionDigits == 0) "0" else "0.00")
                    },
                    isError = priceError != null,
                    supportingText = priceError?.let { error ->
                        { Text(strings.validationMessage(error, currency)) }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (currency.fractionDigits == 0) KeyboardType.Number else KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.orderedByLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (participants.size > 1) {
                            FilterChip(
                                selected = allSelected,
                                onClick = {
                                    selectedIds = if (allSelected) {
                                        participants.firstOrNull()?.id?.let { setOf(it) } ?: emptySet()
                                    } else {
                                        allParticipantIds
                                    }
                                },
                                shape = CircleShape,
                                label = { Text(strings.everyoneChip) },
                            )
                        }

                        participants.forEachIndexed { index, participant ->
                            val displayName = participant.name.trim().ifEmpty {
                                strings.defaultParticipantName(index)
                            }
                            val isSelected = participant.id in selectedIds
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedIds = if (isSelected) {
                                        if (selectedIds.size > 1) selectedIds - participant.id else selectedIds
                                    } else {
                                        selectedIds + participant.id
                                    }
                                },
                                shape = CircleShape,
                                label = { Text(displayName) },
                            )
                        }
                    }

                    if (validMoney != null && selectedIds.size > 1) {
                        val eachAmount = Money(validMoney.minorUnits / selectedIds.size)
                        Text(
                            text = strings.splitWaysHint(
                                count = selectedIds.size,
                                perPersonFormatted = MoneyFormatter.format(eachAmount, currency),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nameText.trim(), priceText, selectedIds) },
                enabled = canSave,
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
    )
}
