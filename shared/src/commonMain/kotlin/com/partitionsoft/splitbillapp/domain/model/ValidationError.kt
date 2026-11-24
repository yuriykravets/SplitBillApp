package com.partitionsoft.splitbillapp.domain.model

sealed interface ValidationError {
    data object EmptyBillAmount : ValidationError
    data object InvalidBillAmount : ValidationError
    data object NonPositiveBillAmount : ValidationError
    data object AmountTooLarge : ValidationError
    data object InvalidTipPercentage : ValidationError
    data object TipPercentageOutOfRange : ValidationError
    data object InvalidAdditionalCharge : ValidationError
    data object NegativeAdditionalCharge : ValidationError
    data object InvalidParticipantCount : ValidationError
    data object NoItemsAdded : ValidationError
    data object UnassignedItems : ValidationError
}
