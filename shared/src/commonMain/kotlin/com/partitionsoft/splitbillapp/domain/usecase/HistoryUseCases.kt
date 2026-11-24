package com.partitionsoft.splitbillapp.domain.usecase

import com.partitionsoft.splitbillapp.core.localization.AppLanguage
import com.partitionsoft.splitbillapp.core.localization.stringsFor
import com.partitionsoft.splitbillapp.core.platform.TimeProvider
import com.partitionsoft.splitbillapp.domain.model.BillCalculationResult
import com.partitionsoft.splitbillapp.domain.model.BillInput
import com.partitionsoft.splitbillapp.domain.model.SavedBillSplit
import com.partitionsoft.splitbillapp.domain.repository.BillHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class ObserveBillHistoryUseCase(
    private val repository: BillHistoryRepository,
) {
    operator fun invoke(): Flow<List<SavedBillSplit>> = repository.observeHistory()
}

class SaveBillSplitUseCase(
    private val repository: BillHistoryRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        input: BillInput,
        result: BillCalculationResult,
        existingId: String? = null,
        language: AppLanguage = AppLanguage.ENGLISH,
    ): SavedBillSplit {
        val now = timeProvider.currentTimeMillis()
        val id = existingId ?: "${now}_${Random.nextInt(1000, 9999)}"
        val strings = stringsFor(language)
        val title = input.note.trim().ifEmpty {
            val named = input.participants.mapNotNull { it.name.trim().ifEmpty { null } }
            when {
                named.isEmpty() -> strings.defaultSplitTitle(input.participants.size)
                named.size <= 3 -> named.joinToString(", ")
                else -> "${named.take(2).joinToString(", ")} +${named.size - 2}"
            }
        }
        val saved = SavedBillSplit(
            id = id,
            createdAtEpochMillis = now,
            title = title,
            input = input,
            result = result,
        )
        repository.save(saved)
        return saved
    }
}

class DeleteBillSplitUseCase(
    private val repository: BillHistoryRepository,
) {
    suspend operator fun invoke(id: String) = repository.deleteById(id)
}

class ClearBillHistoryUseCase(
    private val repository: BillHistoryRepository,
) {
    suspend operator fun invoke() = repository.clearAll()
}
