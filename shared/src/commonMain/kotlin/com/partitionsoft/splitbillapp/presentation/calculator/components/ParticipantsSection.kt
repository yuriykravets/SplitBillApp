package com.partitionsoft.splitbillapp.presentation.calculator.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.partitionsoft.splitbillapp.core.format.MoneyFormatter
import com.partitionsoft.splitbillapp.core.localization.LocalAppStrings
import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.domain.model.BillCalculationResult
import com.partitionsoft.splitbillapp.domain.model.Participant
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.domain.usecase.CalculateBillSplitUseCase
import com.partitionsoft.splitbillapp.presentation.icons.AppIcons

@Composable
fun ParticipantsSection(
    participants: List<Participant>,
    result: BillCalculationResult?,
    splitMode: SplitMode,
    currency: Currency,
    showParticipantNames: Boolean,
    onIncrementPeople: () -> Unit,
    onDecrementPeople: () -> Unit,
    onToggleParticipantNames: () -> Unit,
    onEditParticipantClick: (Participant) -> Unit,
    onQuickAddItemForParticipant: ((Participant) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isByItems = splitMode == SplitMode.BY_ITEMS

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.splitBetweenTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (isByItems) strings.itemizedSplitSubtitle else strings.equalSplitSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Expressive tactile stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledTonalIconButton(
                        onClick = onDecrementPeople,
                        enabled = participants.size > 1,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(
                            imageVector = AppIcons.Remove,
                            contentDescription = strings.decreasePeopleCd,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    AnimatedContent(
                        targetState = participants.size,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "peopleCount",
                    ) { count ->
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum",
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.semantics {
                                contentDescription = strings.peopleCountLabel(count)
                            },
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onIncrementPeople,
                        enabled = participants.size < CalculateBillSplitUseCase.MAX_PARTICIPANTS,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = strings.increasePeopleCd,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            if (!isByItems) {
                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onToggleParticipantNames,
                    modifier = Modifier.align(Alignment.Start),
                ) {
                    Icon(
                        imageVector = AppIcons.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showParticipantNames) strings.hideParticipantNames else strings.assignNames,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            AnimatedVisibility(
                visible = isByItems || showParticipantNames,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    participants.forEachIndexed { index, participant ->
                        val share = result?.participantShares?.getOrNull(index)
                        val itemsSubtitle = if (isByItems) {
                            val labels = share?.assignedItemLabels.orEmpty()
                            if (labels.isNotEmpty()) labels.joinToString(", ") else strings.noOrdersAssigned
                        } else {
                            null
                        }
                        ParticipantRow(
                            index = index,
                            participant = participant,
                            shareAmountText = share?.let {
                                MoneyFormatter.format(it.amount, currency)
                            },
                            hasRemainder = share?.hasRoundingRemainder == true,
                            itemsSubtitle = itemsSubtitle,
                            onClick = { onEditParticipantClick(participant) },
                            onQuickAddItemClick = if (isByItems && onQuickAddItemForParticipant != null) {
                                { onQuickAddItemForParticipant(participant) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    index: Int,
    participant: Participant,
    shareAmountText: String?,
    hasRemainder: Boolean,
    itemsSubtitle: String?,
    onClick: () -> Unit,
    onQuickAddItemClick: (() -> Unit)?,
) {
    val strings = LocalAppStrings.current
    val displayName = participant.name.trim().ifEmpty { strings.defaultParticipantName(index) }
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "${index + 1}"

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = if (onQuickAddItemClick != null) 6.dp else 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (index == 0) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (index == 0) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtitleText = itemsSubtitle ?: if (participant.name.isBlank()) strings.tapToRename else null
                    if (subtitleText != null) {
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (shareAmountText != null) {
                    if (hasRemainder) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            Text(
                                text = "+1",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Text(
                        text = shareAmountText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (onQuickAddItemClick != null) {
                    IconButton(
                        onClick = onQuickAddItemClick,
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = strings.addItem,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditParticipantNameDialog(
    participant: Participant,
    index: Int,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val strings = LocalAppStrings.current
    var nameText by remember(participant.id) { mutableStateOf(participant.name) }
    val displayName = participant.name.trim().ifEmpty { strings.defaultParticipantName(index) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.renameDialogTitle(displayName),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it.take(32) },
                    singleLine = true,
                    placeholder = { Text(strings.renameDialogPlaceholder) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    strings.quickParticipantNames.forEach { preset ->
                        SuggestionChip(
                            onClick = { nameText = preset },
                            label = { Text(preset) },
                            shape = CircleShape,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nameText.trim()) }) {
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
