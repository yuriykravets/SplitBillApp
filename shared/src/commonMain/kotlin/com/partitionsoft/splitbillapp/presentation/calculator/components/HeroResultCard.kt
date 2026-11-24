package com.partitionsoft.splitbillapp.presentation.calculator.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.partitionsoft.splitbillapp.core.model.Money
import com.partitionsoft.splitbillapp.domain.model.BillCalculationResult
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.presentation.icons.AppIcons

@Composable
fun HeroResultCard(
    result: BillCalculationResult?,
    splitMode: SplitMode,
    currency: Currency,
    participantCount: Int,
    canShareOrSave: Boolean,
    isSavedUpToDate: Boolean,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isByItems = splitMode == SplitMode.BY_ITEMS
    val primaryAmountText = if (result != null) {
        val displayMoney = if (isByItems) result.totalAmount else result.baseAmountPerPerson
        MoneyFormatter.format(displayMoney, result.currency)
    } else {
        MoneyFormatter.format(Money.ZERO, currency)
    }

    val perPersonLabel = when {
        isByItems -> strings.totalToPay
        participantCount == 1 -> strings.totalToPay
        result?.hasRemainderDistribution == true -> strings.perPersonFrom
        else -> strings.eachPersonPays
    }

    val sharesRangeText = if (isByItems && result != null && result.participantShares.size > 1) {
        val minShare = result.participantShares.minOf { it.amount }
        val maxShare = result.participantShares.maxOf { it.amount }
        if (minShare != maxShare) {
            strings.sharesRangeLabel(
                minFormatted = MoneyFormatter.format(minShare, result.currency),
                maxFormatted = MoneyFormatter.format(maxShare, result.currency),
            )
        } else {
            null
        }
    } else {
        null
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = perPersonLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = strings.peopleCountLabel(participantCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedContent(
                targetState = primaryAmountText,
                transitionSpec = {
                    (fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 5 }) togetherWith
                        (fadeOut(tween(140)) + slideOutVertically(tween(140)) { -it / 5 })
                },
                label = "perPersonAmount",
            ) { amountText ->
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics {
                        contentDescription = "$perPersonLabel $amountText"
                    },
                )
            }

            AnimatedVisibility(
                visible = sharesRangeText != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                if (sharesRangeText != null) {
                    Text(
                        text = sharesRangeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            AnimatedVisibility(
                visible = !isByItems && result?.hasRemainderDistribution == true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val remainderCount = result?.participantShares?.count { it.hasRoundingRemainder } ?: 0
                val unitLabel = if (currency.fractionDigits == 0) "1 ${currency.symbol}" else "0.01 ${currency.symbol}"
                Text(
                    text = strings.remainderExplanation(remainderCount, unitLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Breakdown strip
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        BreakdownMetric(
                            label = strings.billLabel,
                            value = MoneyFormatter.format(result?.billAmount ?: Money.ZERO, currency),
                            modifier = Modifier.weight(1f),
                        )
                        val tipTitle = if (result != null && result.tipBasisPoints > 0) {
                            "${strings.tipLabel} (${MoneyFormatter.formatPercentage(result.tipBasisPoints)})"
                        } else {
                            strings.tipLabel
                        }
                        BreakdownMetric(
                            label = tipTitle,
                            value = MoneyFormatter.format(result?.tipAmount ?: Money.ZERO, currency),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    AnimatedVisibility(
                        visible = (result?.additionalCharge?.minorUnits ?: 0L) > 0L,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            BreakdownMetric(
                                label = strings.taxAndExtrasLabel,
                                value = MoneyFormatter.format(result?.additionalCharge ?: Money.ZERO, currency),
                                modifier = Modifier.weight(1f),
                            )
                            BreakdownMetric(
                                label = strings.totalLabel,
                                value = MoneyFormatter.format(result?.totalAmount ?: Money.ZERO, currency),
                                emphasized = true,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if ((result?.additionalCharge?.minorUnits ?: 0L) == 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = strings.totalBillLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = MoneyFormatter.format(result?.totalAmount ?: Money.ZERO, currency),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Share + Secondary Save action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onShareClick,
                    enabled = canShareOrSave,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = AppIcons.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = strings.shareSplit, style = MaterialTheme.typography.labelLarge)
                }

                FilledTonalButton(
                    onClick = onSaveClick,
                    enabled = canShareOrSave && !isSavedUpToDate,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    ),
                ) {
                    Icon(
                        imageVector = if (isSavedUpToDate) AppIcons.Check else AppIcons.BookmarkCheck,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSavedUpToDate) strings.saved else strings.save,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = if (emphasized) {
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum",
                )
            } else {
                MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                )
            },
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
