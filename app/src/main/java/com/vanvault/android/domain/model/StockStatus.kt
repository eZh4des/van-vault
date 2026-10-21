package com.vanvault.android.domain.model

/**
 * Domain enum used to drive UI in BOTH screen reader announcements
 * (TalkBack: "3 unidades — Insuficiente") and color roles.
 * Avoids hardcoding strings in Composables (DRY).
 */
enum class StockStatus(val labelKey: String) {
    AVAILABLE("stock_available"),
    LOW_STOCK("stock_low"),
    OUT_OF_STOCK("stock_out")
}