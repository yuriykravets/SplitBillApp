package com.partitionsoft.splitbillapp.domain.model

import com.partitionsoft.splitbillapp.core.model.Money

/**
 * Represents a person participating in splitting the bill.
 * [customShare] is reserved for future unequal/item-level split modes while keeping MVP equal-split clean.
 */
data class Participant(
    val id: String,
    val name: String,
    val customShare: Money? = null,
) {
    fun displayName(index: Int): String = name.trim().ifEmpty {
        if (index == 0) "You" else "Person ${index + 1}"
    }
}
