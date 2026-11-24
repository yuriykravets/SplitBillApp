package com.partitionsoft.splitbillapp.domain.usecase

import com.partitionsoft.splitbillapp.core.format.MoneyFormatter
import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.core.localization.stringsFor
import com.partitionsoft.splitbillapp.domain.model.BillCalculationResult

/**
 * Formats a [BillCalculationResult] into a clean, readable breakdown suitable for
 * sharing via system share sheets or clipboard in the active [AppLanguage].
 */
class FormatShareSummaryUseCase {

    operator fun invoke(
        result: BillCalculationResult,
        note: String = "",
        language: AppLanguage = AppLanguage.ENGLISH,
    ): String {
        val strings = stringsFor(language)
        val currency = result.currency
        val sb = StringBuilder()

        val trimmedNote = note.trim()
        if (trimmedNote.isNotEmpty()) {
            sb.appendLine(trimmedNote)
        }

        sb.appendLine("${strings.shareRestaurantBill}: ${MoneyFormatter.format(result.billAmount, currency, trimWholeZeros = true)}")

        if (result.tipAmount.minorUnits > 0L || result.tipBasisPoints > 0) {
            val pct = MoneyFormatter.formatPercentage(result.tipBasisPoints)
            sb.appendLine("${strings.shareTip} ($pct): ${MoneyFormatter.format(result.tipAmount, currency, trimWholeZeros = true)}")
        } else {
            sb.appendLine("${strings.shareTip}: ${MoneyFormatter.format(result.tipAmount, currency, trimWholeZeros = true)}")
        }

        if (result.additionalCharge.minorUnits > 0L) {
            sb.appendLine("${strings.shareTaxAndExtras}: ${MoneyFormatter.format(result.additionalCharge, currency, trimWholeZeros = true)}")
        }

        sb.appendLine("${strings.shareTotal}: ${MoneyFormatter.format(result.totalAmount, currency, trimWholeZeros = true)}")
        sb.appendLine("${strings.sharePeople}: ${result.participantCount}")

        val hasCustomNames = result.participantShares.any { it.participant.name.isNotBlank() }
        val isByItems = result.splitMode == com.partitionsoft.splitbillapp.domain.model.SplitMode.BY_ITEMS
        if (isByItems || hasCustomNames || result.hasRemainderDistribution) {
            sb.appendLine()
            sb.appendLine("${strings.shareBreakdown}:")
            result.participantShares.forEach { share ->
                val name = share.participant.name.trim().ifEmpty {
                    strings.defaultParticipantName(share.index)
                }
                val formatted = MoneyFormatter.format(share.amount, currency, trimWholeZeros = true)
                if (isByItems && share.assignedItemLabels.isNotEmpty()) {
                    val itemsSummary = share.assignedItemLabels.joinToString(", ")
                    sb.appendLine("• $name: $formatted ($itemsSummary)")
                } else {
                    sb.appendLine("• $name: $formatted")
                }
            }
        } else {
            val each = MoneyFormatter.format(result.baseAmountPerPerson, currency, trimWholeZeros = true)
            sb.append("${strings.shareEachPersonPays}: $each")
        }

        return sb.toString().trimEnd()
    }
}
