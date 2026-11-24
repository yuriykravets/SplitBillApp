package com.partitionsoft.splitbillapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.partitionsoft.splitbillapp.data.mapper.BillHistoryMapper
import com.partitionsoft.splitbillapp.db.SplitBillDatabase
import com.partitionsoft.splitbillapp.domain.model.SavedBillSplit
import com.partitionsoft.splitbillapp.domain.repository.BillHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightBillHistoryRepository(
    private val database: SplitBillDatabase,
    private val mapper: BillHistoryMapper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BillHistoryRepository {

    private val queries get() = database.billHistoryQueries

    override fun observeHistory(): Flow<List<SavedBillSplit>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows -> rows.mapNotNull(mapper::toDomain) }
    }

    override suspend fun save(split: SavedBillSplit) {
        withContext(ioDispatcher) {
            queries.insertOrReplace(
                id = split.id,
                created_at_millis = split.createdAtEpochMillis,
                title = split.title,
                note = split.input.note,
                currency_code = split.input.currency.code,
                bill_amount_minor = split.input.billAmount.minorUnits,
                tip_basis_points = split.input.tipBasisPoints.toLong(),
                additional_charge_minor = split.input.additionalCharge.minorUnits,
                participants_json = mapper.encodePayload(split.input),
            )
        }
    }

    override suspend fun deleteById(id: String) {
        withContext(ioDispatcher) {
            queries.deleteById(id)
        }
    }

    override suspend fun clearAll() {
        withContext(ioDispatcher) {
            queries.deleteAll()
        }
    }
}
