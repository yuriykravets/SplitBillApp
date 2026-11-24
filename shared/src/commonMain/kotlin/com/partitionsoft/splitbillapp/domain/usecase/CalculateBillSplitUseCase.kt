package com.partitionsoft.splitbillapp.domain.usecase

import com.partitionsoft.splitbillapp.core.model.Money
import com.partitionsoft.splitbillapp.domain.model.BillCalculationResult
import com.partitionsoft.splitbillapp.domain.model.BillInput
import com.partitionsoft.splitbillapp.domain.model.ParticipantShare
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.domain.model.ValidationError

sealed interface CalculationOutcome {
    data class Success(val result: BillCalculationResult) : CalculationOutcome
    data class Failure(val error: ValidationError) : CalculationOutcome
}

/**
 * Pure business-logic use case that computes the restaurant bill split using exact
 * integer minor-unit arithmetic. Guarantees that the sum of individual participant
 * shares always equals [BillCalculationResult.totalAmount] in both EQUAL and BY_ITEMS modes.
 */
class CalculateBillSplitUseCase {

    operator fun invoke(input: BillInput): CalculationOutcome {
        if (input.tipBasisPoints < 0 || input.tipBasisPoints > ParseInputUseCase.MAX_TIP_BASIS_POINTS) {
            return CalculationOutcome.Failure(ValidationError.TipPercentageOutOfRange)
        }
        if (input.additionalCharge.minorUnits < 0L) {
            return CalculationOutcome.Failure(ValidationError.NegativeAdditionalCharge)
        }
        if (input.participants.isEmpty() || input.participants.size > MAX_PARTICIPANTS) {
            return CalculationOutcome.Failure(ValidationError.InvalidParticipantCount)
        }

        return when (input.splitMode) {
            SplitMode.EQUAL -> calculateEqual(input)
            SplitMode.BY_ITEMS -> calculateByItems(input)
        }
    }

    private fun calculateEqual(input: BillInput): CalculationOutcome {
        if (input.billAmount.minorUnits <= 0L) {
            return CalculationOutcome.Failure(ValidationError.NonPositiveBillAmount)
        }

        val tipAmount = input.billAmount.percentageOfBasisPoints(input.tipBasisPoints)
        val totalAmount = input.billAmount + tipAmount + input.additionalCharge

        val count = input.participants.size
        val basePerPerson = Money(totalAmount.minorUnits / count)
        val distributed = totalAmount.splitEvenly(count)
        val hasRemainder = (totalAmount.minorUnits % count) != 0L

        val shares = input.participants.mapIndexed { index, participant ->
            val shareAmount = distributed[index]
            ParticipantShare(
                participant = participant,
                index = index,
                amount = shareAmount,
                hasRoundingRemainder = shareAmount.minorUnits > basePerPerson.minorUnits,
            )
        }

        return CalculationOutcome.Success(
            BillCalculationResult(
                currency = input.currency,
                billAmount = input.billAmount,
                tipAmount = tipAmount,
                tipBasisPoints = input.tipBasisPoints,
                additionalCharge = input.additionalCharge,
                totalAmount = totalAmount,
                baseAmountPerPerson = basePerPerson,
                participantShares = shares,
                hasRemainderDistribution = hasRemainder,
                splitMode = SplitMode.EQUAL,
                items = emptyList(),
            )
        )
    }

    private fun calculateByItems(input: BillInput): CalculationOutcome {
        if (input.items.isEmpty()) {
            return CalculationOutcome.Failure(ValidationError.NoItemsAdded)
        }

        val participantIndexById = input.participants.withIndex().associate { it.value.id to it.index }
        val count = input.participants.size
        val subtotals = LongArray(count)
        val itemLabels = Array(count) { mutableListOf<String>() }

        var totalBillMinor = 0L
        for (item in input.items) {
            if (item.price.minorUnits <= 0L) {
                return CalculationOutcome.Failure(ValidationError.NonPositiveBillAmount)
            }
            val assignedIndices = item.assignedParticipantIds
                .mapNotNull { participantIndexById[it] }
                .sorted()
            if (assignedIndices.isEmpty()) {
                return CalculationOutcome.Failure(ValidationError.UnassignedItems)
            }

            totalBillMinor += item.price.minorUnits
            val splits = item.price.splitEvenly(assignedIndices.size)
            val label = if (assignedIndices.size > 1) {
                "${item.name.trim()} (1/${assignedIndices.size})"
            } else {
                item.name.trim()
            }

            assignedIndices.forEachIndexed { splitIdx, participantIdx ->
                subtotals[participantIdx] += splits[splitIdx].minorUnits
                if (label.isNotEmpty()) {
                    itemLabels[participantIdx].add(label)
                }
            }
        }

        val billAmount = Money(totalBillMinor)
        val tipAmount = billAmount.percentageOfBasisPoints(input.tipBasisPoints)
        val totalAmount = billAmount + tipAmount + input.additionalCharge

        val tipShares = distributeProportionally(tipAmount, subtotals)
        val extraShares = distributeProportionally(input.additionalCharge, subtotals)

        val shares = input.participants.mapIndexed { index, participant ->
            val itemsSub = Money(subtotals[index])
            val tipShare = tipShares[index]
            val extraShare = extraShares[index]
            val totalShare = itemsSub + tipShare + extraShare
            ParticipantShare(
                participant = participant,
                index = index,
                amount = totalShare,
                hasRoundingRemainder = false,
                itemsSubtotal = itemsSub,
                tipShare = tipShare,
                additionalChargeShare = extraShare,
                assignedItemLabels = itemLabels[index].toList(),
            )
        }

        val minShare = shares.minOfOrNull { it.amount } ?: Money.ZERO

        return CalculationOutcome.Success(
            BillCalculationResult(
                currency = input.currency,
                billAmount = billAmount,
                tipAmount = tipAmount,
                tipBasisPoints = input.tipBasisPoints,
                additionalCharge = input.additionalCharge,
                totalAmount = totalAmount,
                baseAmountPerPerson = minShare,
                participantShares = shares,
                hasRemainderDistribution = false,
                splitMode = SplitMode.BY_ITEMS,
                items = input.items,
            )
        )
    }

    /**
     * Distributes [total] across participants proportionally to [weights] using the
     * Largest Remainder (Hamilton) method so that the sum of returned amounts always equals [total].
     */
    private fun distributeProportionally(total: Money, weights: LongArray): List<Money> {
        val n = weights.size
        if (total.minorUnits == 0L || n == 0) {
            return List(n) { Money.ZERO }
        }
        val weightSum = weights.sum()
        if (weightSum <= 0L) {
            return total.splitEvenly(n)
        }

        val totalUnits = total.minorUnits
        val floors = LongArray(n)
        val remainders = DoubleArray(n)
        var allocated = 0L

        val safeToMultiply = totalUnits <= Long.MAX_VALUE / weightSum
        for (i in 0 until n) {
            val w = weights[i]
            if (w <= 0L) continue
            if (safeToMultiply) {
                val product = totalUnits * w
                floors[i] = product / weightSum
                remainders[i] = (product % weightSum).toDouble()
            } else {
                val exact = totalUnits.toDouble() * (w.toDouble() / weightSum.toDouble())
                val floorVal = exact.toLong()
                floors[i] = floorVal
                remainders[i] = exact - floorVal.toDouble()
            }
            allocated += floors[i]
        }

        var remaining = (totalUnits - allocated).toInt()
        if (remaining > 0) {
            val order = (0 until n)
                .filter { weights[it] > 0L }
                .sortedWith(compareByDescending<Int> { remainders[it] }.thenBy { it })
            var idx = 0
            while (remaining > 0 && order.isNotEmpty()) {
                floors[order[idx % order.size]] += 1L
                remaining--
                idx++
            }
        } else if (remaining < 0) {
            val order = (0 until n)
                .filter { floors[it] > 0L }
                .sortedWith(compareBy<Int> { remainders[it] }.thenByDescending { it })
            var idx = 0
            while (remaining < 0 && order.isNotEmpty()) {
                val target = order[idx % order.size]
                if (floors[target] > 0L) {
                    floors[target] -= 1L
                    remaining++
                }
                idx++
            }
        }

        return List(n) { Money(floors[it]) }
    }

    companion object {
        const val MAX_PARTICIPANTS = 50
    }
}
