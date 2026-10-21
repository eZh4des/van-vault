package com.vanvault.android.domain.model

import com.vanvault.android.domain.model.Money

/**
 * Domain entity representing a product in the inventory.
 * Encapsulates business rules (DRY: stock status, formatted price).
 *
 * Pragmatic tip: "Code is the design" — put the *behavior* here, not in Composables.
 */
data class Product(
    val id: String,
    val name: String,
    val modelOrBarcode: String,
    val quantity: Int,
    val unitPrice: Money
) {
    /** Single source of truth for stock classification (Design by Contract). */
    val stockStatus: StockStatus
        get() = when {
            quantity <= 0 -> StockStatus.OUT_OF_STOCK
            quantity < LOW_STOCK_THRESHOLD -> StockStatus.LOW_STOCK
            else -> StockStatus.AVAILABLE
        }

    companion object {
        const val LOW_STOCK_THRESHOLD = 5
    }
}