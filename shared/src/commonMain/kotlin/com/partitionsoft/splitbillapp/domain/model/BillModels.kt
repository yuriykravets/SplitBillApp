package com.partitionsoft.splitbillapp.domain.model

import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money

enum class SplitMode {
    EQUAL,
    BY_ITEMS,
}

data class BillItem(
    val id: String,
    val name: String,
    val price: Money,
    val assignedParticipantIds: Set<String>,
)

data class BillInput(
    val billAmount: Money,
    val currency: Currency,
    val tipBasisPoints: Int,
    val additionalCharge: Money = Money.ZERO,
    val participants: List<Participant>,
    val note: String = "",
    val splitMode: SplitMode = SplitMode.EQUAL,
    val items: List<BillItem> = emptyList(),
)

data class ParticipantShare(
    val participant: Participant,
    val index: Int,
    val amount: Money,
    val hasRoundingRemainder: Boolean,
    val itemsSubtotal: Money = Money.ZERO,
    val tipShare: Money = Money.ZERO,
    val additionalChargeShare: Money = Money.ZERO,
    val assignedItemLabels: List<String> = emptyList(),
)

data class BillCalculationResult(
    val currency: Currency,
    val billAmount: Money,
    val tipAmount: Money,
    val tipBasisPoints: Int,
    val additionalCharge: Money,
    val totalAmount: Money,
    val baseAmountPerPerson: Money,
    val participantShares: List<ParticipantShare>,
    val hasRemainderDistribution: Boolean,
    val splitMode: SplitMode = SplitMode.EQUAL,
    val items: List<BillItem> = emptyList(),
) {
    val participantCount: Int get() = participantShares.size
}

data class SavedBillSplit(
    val id: String,
    val createdAtEpochMillis: Long,
    val title: String,
    val input: BillInput,
    val result: BillCalculationResult,
)
