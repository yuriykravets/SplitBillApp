package com.partitionsoft.splitbillapp.domain.repository

import com.partitionsoft.splitbillapp.domain.model.SavedBillSplit
import kotlinx.coroutines.flow.Flow

interface BillHistoryRepository {
    fun observeHistory(): Flow<List<SavedBillSplit>>
    suspend fun save(split: SavedBillSplit)
    suspend fun deleteById(id: String)
    suspend fun clearAll()
}
