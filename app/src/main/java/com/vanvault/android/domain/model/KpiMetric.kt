package com.vanvault.android.domain.model

/**
 * KPI (Key Performance Indicator) genérico del dashboard.
 *
 * Pragmatic tip: usamos una data class simple (no sealed) porque el dominio
 * es agnóstico del componente UI que lo muestre. DRY: sirve para Dashboard,
 * ROI y Admin.
 */
data class KpiMetric(
    val id: String,
    val label: String,
    val value: String,
    val trend: Trend,
    val icon: String,    // nombre del icono Material
    val accentColor: AccentColor = AccentColor.NEUTRAL
) {
    enum class Trend { UP, DOWN, FLAT }
    enum class AccentColor { PRIMARY, SUCCESS, WARNING, ERROR, NEUTRAL }
}

/**
 * Snapshot agregado para el Dashboard.
 * Cumple el principio de "single fetch, single render" (Google Architecture).
 */
data class DashboardSnapshot(
    val greeting: String,
    val userName: String,
    val primaryMetrics: List<KpiMetric>,
    val secondaryMetrics: List<KpiMetric>,
    val recentMovements: List<RecentMovement>
)

data class RecentMovement(
    val id: String,
    val productName: String,
    val type: MovementType,
    val quantity: Int,
    val timestampLabel: String,  // ej. "Hace 2 horas"
    val unitPrice: Money?
) {
    enum class MovementType { SALE, RESTOCK, ADJUSTMENT, REMOVAL }
}