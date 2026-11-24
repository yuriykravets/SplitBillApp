package com.partitionsoft.splitbillapp

import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money
import com.partitionsoft.splitbillapp.domain.model.BillInput
import com.partitionsoft.splitbillapp.domain.model.BillItem
import com.partitionsoft.splitbillapp.domain.model.Participant
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.domain.model.ValidationError
import com.partitionsoft.splitbillapp.domain.usecase.CalculateBillSplitUseCase
import com.partitionsoft.splitbillapp.domain.usecase.CalculationOutcome
import com.partitionsoft.splitbillapp.domain.usecase.FormatShareSummaryUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ParseInputUseCase
import com.partitionsoft.splitbillapp.domain.usecase.ParseMoneyResult
import com.partitionsoft.splitbillapp.domain.usecase.ParseTipResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CalculateBillSplitUseCaseTest {

    private val calculate = CalculateBillSplitUseCase()
    private val parse = ParseInputUseCase()
    private val formatShare = FormatShareSummaryUseCase()

    @Test
    fun calculatesStandardRestaurantBillCorrectly() {
        val input = BillInput(
            billAmount = Money(12000), // $120.00
            currency = Currency.USD,
            tipBasisPoints = 1500, // 15%
            additionalCharge = Money.ZERO,
            participants = List(3) { Participant(id = "p_$it", name = "") },
        )

        val outcome = assertIs<CalculationOutcome.Success>(calculate(input))
        val result = outcome.result

        assertEquals(Money(1800), result.tipAmount)
        assertEquals(Money(13800), result.totalAmount)
        assertEquals(Money(4600), result.baseAmountPerPerson)
        assertFalse(result.hasRemainderDistribution)
        assertEquals(listOf(Money(4600), Money(4600), Money(4600)), result.participantShares.map { it.amount })
    }

    @Test
    fun distributesIndivisibleCentsFairlyWithoutLosingMoney() {
        val input = BillInput(
            billAmount = Money(10000), // $100.00
            currency = Currency.USD,
            tipBasisPoints = 0,
            participants = listOf(
                Participant("1", "Alex"),
                Participant("2", "Maria"),
                Participant("3", "John"),
            ),
        )

        val outcome = assertIs<CalculationOutcome.Success>(calculate(input))
        val result = outcome.result

        assertTrue(result.hasRemainderDistribution)
        assertEquals(Money(3333), result.baseAmountPerPerson)
        assertEquals(Money(3334), result.participantShares[0].amount)
        assertTrue(result.participantShares[0].hasRoundingRemainder)
        assertEquals(Money(3333), result.participantShares[1].amount)
        assertFalse(result.participantShares[1].hasRoundingRemainder)
        assertEquals(Money(3333), result.participantShares[2].amount)
        assertEquals(result.totalAmount.minorUnits, result.participantShares.sumOf { it.amount.minorUnits })
    }

    @Test
    fun includesAdditionalChargesInTotalAndPerPersonSplit() {
        val input = BillInput(
            billAmount = Money(10000), // $100.00
            currency = Currency.USD,
            tipBasisPoints = 1000, // $10.00 tip
            additionalCharge = Money(800), // $8.00 tax
            participants = List(4) { Participant(id = "p_$it", name = "") },
        )

        val outcome = assertIs<CalculationOutcome.Success>(calculate(input))
        val result = outcome.result

        assertEquals(Money(1000), result.tipAmount)
        assertEquals(Money(800), result.additionalCharge)
        assertEquals(Money(11800), result.totalAmount)
        assertEquals(Money(2950), result.baseAmountPerPerson)
    }

    @Test
    fun rejectsInvalidInputs() {
        val validParticipants = listOf(Participant("1", "You"))

        assertEquals(
            CalculationOutcome.Failure(ValidationError.NonPositiveBillAmount),
            calculate(BillInput(Money.ZERO, Currency.USD, 1500, Money.ZERO, validParticipants)),
        )
        assertEquals(
            CalculationOutcome.Failure(ValidationError.NonPositiveBillAmount),
            calculate(BillInput(Money(-500), Currency.USD, 1500, Money.ZERO, validParticipants)),
        )
        assertEquals(
            CalculationOutcome.Failure(ValidationError.InvalidParticipantCount),
            calculate(BillInput(Money(5000), Currency.USD, 1500, Money.ZERO, emptyList())),
        )
        assertEquals(
            CalculationOutcome.Failure(ValidationError.NegativeAdditionalCharge),
            calculate(BillInput(Money(5000), Currency.USD, 1500, Money(-100), validParticipants)),
        )
        assertEquals(
            CalculationOutcome.Failure(ValidationError.TipPercentageOutOfRange),
            calculate(BillInput(Money(5000), Currency.USD, -100, Money.ZERO, validParticipants)),
        )
    }

    @Test
    fun parsesValidAndInvalidUserInputStrings() {
        assertEquals(ParseMoneyResult.Valid(Money(12050)), parse.parseBillAmount("120.50", Currency.USD))
        assertEquals(ParseMoneyResult.Valid(Money(12050)), parse.parseBillAmount("120,5", Currency.USD))
        assertEquals(ParseMoneyResult.Valid(Money(3500)), parse.parseBillAmount("3500", Currency.JPY))
        assertEquals(
            ParseMoneyResult.Invalid(ValidationError.InvalidBillAmount),
            parse.parseBillAmount("35.5", Currency.JPY),
        )
        assertEquals(
            ParseMoneyResult.Invalid(ValidationError.EmptyBillAmount),
            parse.parseBillAmount("   ", Currency.USD),
        )
        assertEquals(
            ParseMoneyResult.Invalid(ValidationError.NonPositiveBillAmount),
            parse.parseBillAmount("0", Currency.USD),
        )
        assertEquals(
            ParseMoneyResult.Invalid(ValidationError.NonPositiveBillAmount),
            parse.parseBillAmount("-15", Currency.USD),
        )

        assertEquals(ParseTipResult.Valid(1250), parse.parseTipPercentage("12.5"))
        assertEquals(ParseTipResult.Valid(0), parse.parseTipPercentage(""))
        assertEquals(
            ParseTipResult.Invalid(ValidationError.TipPercentageOutOfRange),
            parse.parseTipPercentage("250"),
        )
    }

    @Test
    fun formatsCleanShareSummary() {
        val input = BillInput(
            billAmount = Money(12000),
            currency = Currency.USD,
            tipBasisPoints = 1500,
            participants = List(3) { Participant("p_$it", "") },
        )
        val result = assertIs<CalculationOutcome.Success>(calculate(input)).result

        val text = formatShare(result)
        assertTrue(text.contains("Restaurant bill: $120"))
        assertTrue(text.contains("Tip (15%): $18"))
        assertTrue(text.contains("Total: $138"))
        assertTrue(text.contains("People: 3"))
        assertTrue(text.contains("Each person pays: $46"))
    }

    @Test
    fun calculatesItemizedSplitWithProportionalTipAndExtras() {
        val participants = listOf(
            Participant("p_1", "You"),
            Participant("p_2", "Alex"),
        )
        val items = listOf(
            BillItem("i_1", "Tea", Money(400), setOf("p_1")),           // $4.00
            BillItem("i_2", "Spaghetti", Money(1800), setOf("p_2")),    // $18.00
            BillItem("i_3", "Wine", Money(1200), setOf("p_2")),         // $12.00
        )
        val input = BillInput(
            billAmount = Money(3400),
            currency = Currency.USD,
            tipBasisPoints = 1500, // 15% -> $5.10 total tip ($0.60 + $4.50)
            additionalCharge = Money.ZERO,
            participants = participants,
            splitMode = SplitMode.BY_ITEMS,
            items = items,
        )

        val outcome = assertIs<CalculationOutcome.Success>(calculate(input))
        val result = outcome.result

        assertEquals(SplitMode.BY_ITEMS, result.splitMode)
        assertEquals(Money(3400), result.billAmount)
        assertEquals(Money(510), result.tipAmount)
        assertEquals(Money(3910), result.totalAmount)

        val youShare = result.participantShares[0]
        val alexShare = result.participantShares[1]

        assertEquals(Money(400), youShare.itemsSubtotal)
        assertEquals(Money(60), youShare.tipShare)
        assertEquals(Money(460), youShare.amount)
        assertEquals(listOf("Tea"), youShare.assignedItemLabels)

        assertEquals(Money(3000), alexShare.itemsSubtotal)
        assertEquals(Money(450), alexShare.tipShare)
        assertEquals(Money(3450), alexShare.amount)
        assertEquals(listOf("Spaghetti", "Wine"), alexShare.assignedItemLabels)

        assertEquals(result.totalAmount.minorUnits, result.participantShares.sumOf { it.amount.minorUnits })

        val summary = formatShare(result)
        assertTrue(summary.contains("• You: $4.60 (Tea)"))
        assertTrue(summary.contains("• Alex: $34.50 (Spaghetti, Wine)"))
    }

    @Test
    fun calculatesSharedItemsAndPreservesExactTotalAcrossRounding() {
        val participants = listOf(
            Participant("p_1", "You"),
            Participant("p_2", "Alex"),
            Participant("p_3", "Maria"),
        )
        val items = listOf(
            BillItem("i_1", "Shared Pizza", Money(1000), setOf("p_1", "p_2", "p_3")), // $10 / 3
            BillItem("i_2", "Wine", Money(2000), setOf("p_2")),                       // $20 for Alex
        )
        val input = BillInput(
            billAmount = Money(3000),
            currency = Currency.USD,
            tipBasisPoints = 1250, // 12.5% of $30 = $3.75
            additionalCharge = Money(500), // $5.00 service fee
            participants = participants,
            splitMode = SplitMode.BY_ITEMS,
            items = items,
        )

        val outcome = assertIs<CalculationOutcome.Success>(calculate(input))
        val result = outcome.result

        assertEquals(Money(3000), result.billAmount)
        assertEquals(Money(375), result.tipAmount)
        assertEquals(Money(500), result.additionalCharge)
        assertEquals(Money(3875), result.totalAmount)
        assertEquals(result.totalAmount.minorUnits, result.participantShares.sumOf { it.amount.minorUnits })
        assertEquals(result.tipAmount.minorUnits, result.participantShares.sumOf { it.tipShare.minorUnits })
        assertEquals(result.additionalCharge.minorUnits, result.participantShares.sumOf { it.additionalChargeShare.minorUnits })
        assertTrue(result.participantShares[1].amount > result.participantShares[0].amount)
        assertTrue(result.participantShares[0].assignedItemLabels.contains("Shared Pizza (1/3)"))
    }

    @Test
    fun rejectsEmptyOrUnassignedItemsInByItemsMode() {
        val participants = listOf(Participant("p_1", "You"))
        assertEquals(
            CalculationOutcome.Failure(ValidationError.NoItemsAdded),
            calculate(
                BillInput(
                    billAmount = Money.ZERO,
                    currency = Currency.USD,
                    tipBasisPoints = 1500,
                    participants = participants,
                    splitMode = SplitMode.BY_ITEMS,
                    items = emptyList(),
                )
            ),
        )
        assertEquals(
            CalculationOutcome.Failure(ValidationError.UnassignedItems),
            calculate(
                BillInput(
                    billAmount = Money(500),
                    currency = Currency.USD,
                    tipBasisPoints = 1500,
                    participants = participants,
                    splitMode = SplitMode.BY_ITEMS,
                    items = listOf(BillItem("i_1", "Tea", Money(500), emptySet())),
                )
            ),
        )
    }
}
