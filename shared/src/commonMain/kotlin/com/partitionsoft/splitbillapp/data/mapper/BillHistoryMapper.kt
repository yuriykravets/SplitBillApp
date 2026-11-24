package com.partitionsoft.splitbillapp.data.mapper

import com.partitionsoft.splitbillapp.core.model.Currency
import com.partitionsoft.splitbillapp.core.model.Money
import com.partitionsoft.splitbillapp.db.Bill_history
import com.partitionsoft.splitbillapp.domain.model.BillInput
import com.partitionsoft.splitbillapp.domain.model.BillItem
import com.partitionsoft.splitbillapp.domain.model.Participant
import com.partitionsoft.splitbillapp.domain.model.SavedBillSplit
import com.partitionsoft.splitbillapp.domain.model.SplitMode
import com.partitionsoft.splitbillapp.domain.usecase.CalculateBillSplitUseCase
import com.partitionsoft.splitbillapp.domain.usecase.CalculationOutcome
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class ParticipantDto(
    val id: String,
    val name: String,
    val customShareMinor: Long? = null,
)

@Serializable
private data class BillItemDto(
    val id: String,
    val name: String,
    val priceMinor: Long,
    val assignedParticipantIds: List<String> = emptyList(),
)

@Serializable
private data class BillPayloadDto(
    val splitMode: String = SplitMode.EQUAL.name,
    val participants: List<ParticipantDto> = emptyList(),
    val items: List<BillItemDto> = emptyList(),
)

class BillHistoryMapper(
    private val calculateBillSplit: CalculateBillSplitUseCase,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {

    fun encodePayload(input: BillInput): String {
        val payload = BillPayloadDto(
            splitMode = input.splitMode.name,
            participants = input.participants.map {
                ParticipantDto(
                    id = it.id,
                    name = it.name,
                    customShareMinor = it.customShare?.minorUnits,
                )
            },
            items = input.items.map {
                BillItemDto(
                    id = it.id,
                    name = it.name,
                    priceMinor = it.price.minorUnits,
                    assignedParticipantIds = it.assignedParticipantIds.toList(),
                )
            },
        )
        return json.encodeToString(payload)
    }

    fun toDomain(entity: Bill_history): SavedBillSplit? {
        val raw = entity.participants_json.trimStart()
        val (splitMode, participants, items) = if (raw.startsWith("{")) {
            runCatching {
                val payload = json.decodeFromString<BillPayloadDto>(raw)
                val mode = SplitMode.entries.firstOrNull { it.name == payload.splitMode } ?: SplitMode.EQUAL
                val mappedParticipants = payload.participants.map { dto ->
                    Participant(
                        id = dto.id,
                        name = dto.name,
                        customShare = dto.customShareMinor?.let(::Money),
                    )
                }
                val mappedItems = payload.items.map { dto ->
                    BillItem(
                        id = dto.id,
                        name = dto.name,
                        price = Money(dto.priceMinor),
                        assignedParticipantIds = dto.assignedParticipantIds.toSet(),
                    )
                }
                Triple(mode, mappedParticipants, mappedItems)
            }.getOrDefault(Triple(SplitMode.EQUAL, emptyList(), emptyList()))
        } else {
            val legacyParticipants = runCatching {
                json.decodeFromString<List<ParticipantDto>>(raw).map { dto ->
                    Participant(
                        id = dto.id,
                        name = dto.name,
                        customShare = dto.customShareMinor?.let(::Money),
                    )
                }
            }.getOrDefault(emptyList())
            Triple(SplitMode.EQUAL, legacyParticipants, emptyList())
        }

        val safeParticipants = participants.ifEmpty {
            listOf(Participant(id = "p_1", name = ""))
        }

        val input = BillInput(
            billAmount = Money(entity.bill_amount_minor),
            currency = Currency.fromCode(entity.currency_code),
            tipBasisPoints = entity.tip_basis_points.toInt(),
            additionalCharge = Money(entity.additional_charge_minor),
            participants = safeParticipants,
            note = entity.note,
            splitMode = splitMode,
            items = items,
        )

        return when (val outcome = calculateBillSplit(input)) {
            is CalculationOutcome.Success -> SavedBillSplit(
                id = entity.id,
                createdAtEpochMillis = entity.created_at_millis,
                title = entity.title,
                input = input,
                result = outcome.result,
            )
            is CalculationOutcome.Failure -> null
        }
    }
}
